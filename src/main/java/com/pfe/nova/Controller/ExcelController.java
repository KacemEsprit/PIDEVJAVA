package com.pfe.nova.Controller;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import com.pfe.nova.models.Medication;

public class ExcelController {

    public String exportMedicationsToExcel(List<Medication> medications) {
        if (medications == null) {
            medications = new ArrayList<>();
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Inventaire Médicaments");
            sheet.setDisplayGridlines(false);
            sheet.setZoom(100);
            sheet.setDefaultRowHeight((short) 60);

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle priceStyle = createPriceStyle(workbook);
            CellStyle alternateRowStyle = createAlternateRowStyle(workbook);

            // Titre
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(60);
            Cell titleCell = titleRow.createCell(2);
            titleCell.setCellValue("INVENTAIRE DES MÉDICAMENTS");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 6));

            // Date
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(2);
            dateCell.setCellValue("Exporté le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            dateCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 6));

            // En-tête
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(30);
            String[] headers = {"Image", "Nom", "Prix (DT)", "Quantité", "Description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i + 2);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            sheet.setColumnWidth(2, 6000);

            // Données
            int rowNum = 4;
            boolean isEvenRow = false;

            for (Medication med : medications) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(120);
                CellStyle currentRowStyle = isEvenRow ? alternateRowStyle : dataStyle;

                // Image uniquement (ne pas afficher l'URL)
                if (med.getImagePath() != null && !med.getImagePath().isEmpty()) {
                    try {
                        String imagePath = med.getImagePath().replace("http://localhost/img/", "C:/xampp/htdocs/img/");
                        File file = new File(imagePath);
                        if (file.exists()) {
                            byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());

                            int imageFormat;
                            if (imagePath.toLowerCase().endsWith(".png")) {
                                imageFormat = Workbook.PICTURE_TYPE_PNG;
                            } else {
                                imageFormat = Workbook.PICTURE_TYPE_JPEG;
                            }

                            int pictureIdx = workbook.addPicture(imageBytes, imageFormat);
                            CreationHelper helper = workbook.getCreationHelper();
                            Drawing<?> drawing = sheet.createDrawingPatriarch();
                            ClientAnchor anchor = helper.createClientAnchor();
                            anchor.setCol1(2);
                            anchor.setRow1(rowNum);
                            anchor.setCol2(3);
                            anchor.setRow2(rowNum + 1);

                            Picture pict = drawing.createPicture(anchor, pictureIdx);
                            pict.resize(0.8);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur ajout image : " + e.getMessage());
                    }
                }

                createCell(row, 3, med.getNom(), currentRowStyle);
                createCell(row, 4, med.getPrix(), priceStyle);
                createCell(row, 5, med.getQuantiteStock(), currentRowStyle);
                createCell(row, 6, med.getDescription(), currentRowStyle);

                isEvenRow = !isEvenRow;
                rowNum++;
            }

            for (int i = 2; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            String fileName = "Inventaire_Medicaments_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
                showSuccessAlert(fileName);
            }
            return fileName;
        } catch (IOException e) {
            showErrorAlert(e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur inattendue : " + e.getMessage());
        }
        return null;
    }

    public List<Medication> importMedicationsFromExcel(String filePath) {
        List<Medication> medications = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 4; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Medication med = new Medication();

                    // Image path
                   // Image path (colonne C - index 2)
Cell imageCell = row.getCell(2);
if (imageCell != null && imageCell.getCellType() == CellType.STRING) {
    String localImagePath = imageCell.getStringCellValue().trim();
    File sourceFile = new File(localImagePath);

    if (sourceFile.exists()) {
        // Créer le dossier cible s'il n'existe pas
        File destinationDir = new File("C:/xampp/htdocs/img/");
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // Générer un nom unique pour éviter les conflits
        String extension = "";
        int dotIndex = sourceFile.getName().lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = sourceFile.getName().substring(dotIndex);
        }
        String newFileName = System.currentTimeMillis() + extension;
        File destinationFile = new File(destinationDir, newFileName);

        // Copier le fichier image
        java.nio.file.Files.copy(
            sourceFile.toPath(),
            destinationFile.toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        // Maintenant, set l'URL correct
        String finalImageUrl = "http://localhost/img/" + newFileName;
        med.setImagePath(finalImageUrl);
    } else {
        System.err.println("Image non trouvée : " + localImagePath);
        med.setImagePath(null); // Pas d'image
    }
}


                    // Nom
                    Cell nomCell = row.getCell(3);
                    if (nomCell != null && nomCell.getCellType() == CellType.STRING) {
                        med.setNom(nomCell.getStringCellValue());
                    }

                    // Prix
                    Cell prixCell = row.getCell(4);
                    if (prixCell != null) {
                        if (prixCell.getCellType() == CellType.NUMERIC) {
                            med.setPrix(prixCell.getNumericCellValue());
                        } else if (prixCell.getCellType() == CellType.STRING) {
                            String prixStr = prixCell.getStringCellValue().replace(" DT", "").trim();
                            med.setPrix(Double.parseDouble(prixStr));
                        }
                    }

                    // Quantité
                    Cell quantiteCell = row.getCell(5);
                    if (quantiteCell != null) {
                        if (quantiteCell.getCellType() == CellType.NUMERIC) {
                            med.setQuantiteStock((int) quantiteCell.getNumericCellValue());
                        } else if (quantiteCell.getCellType() == CellType.STRING) {
                            med.setQuantiteStock(Integer.parseInt(quantiteCell.getStringCellValue().trim()));
                        }
                    }

                    // Description
                    Cell descCell = row.getCell(6);
                    if (descCell != null && descCell.getCellType() == CellType.STRING) {
                        med.setDescription(descCell.getStringCellValue());
                    }

                    medications.add(med);
                }
            }

            showSuccessAlert("Import réussi : " + medications.size() + " médicaments importés.");

        } catch (IOException e) {
            showErrorAlert("Erreur lors de la lecture du fichier : " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Une erreur inattendue : " + e.getMessage());
        }

        return medications;
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof LocalDate) {
                cell.setCellValue(Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            }
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(24);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setAllBorders(style, BorderStyle.MEDIUM);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeight(14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setAllBorders(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setAllBorders(style, BorderStyle.THIN);
        return style;
    }

    private CellStyle createAlternateRowStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDateStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private CellStyle createPriceStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"DT\""));
        return style;
    }

    private void setAllBorders(CellStyle style, BorderStyle borderStyle) {
        style.setBorderBottom(borderStyle);
        style.setBorderTop(borderStyle);
        style.setBorderRight(borderStyle);
        style.setBorderLeft(borderStyle);
    }

    private void showSuccessAlert(String fileName) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Fichier généré :\n" + fileName);
            alert.showAndWait();
        });
    }

    private void showErrorAlert(String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur :\n" + error);
            alert.showAndWait();
        });
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFileName.substring(dotIndex);
        }
        String baseName = originalFileName.substring(0, dotIndex);
        String timestamp = String.valueOf(System.currentTimeMillis());
        return baseName + "_" + timestamp + extension;
    }
}
