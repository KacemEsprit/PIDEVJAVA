package com.pfe.nova.Controller;

import com.pfe.nova.configuration.PostDAO;
import com.pfe.nova.configuration.CommentDAO;
import com.pfe.nova.configuration.LikeDAO;
import com.pfe.nova.models.Post;
import com.pfe.nova.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;
import javafx.application.Platform;

public class PostStatisticsController {
    @FXML private Text totalPostsLabel;
    @FXML private Text approvedPostsLabel;
    @FXML private Text pendingPostsLabel;
    @FXML private Text mostActiveCategoryLabel;

    @FXML private Text totalPostsTrend;
    @FXML private Text approvedPostsTrend;
    @FXML private Text pendingPostsTrend;

    @FXML private PieChart categoriesChart;
    @FXML private PieChart statusChart;
    @FXML private AreaChart<String, Number> monthlyPostsChart;
    @FXML private BarChart<String, Number> engagementChart;

    @FXML private FlowPane topPostsContainer;

    @FXML private Label engagementCategoryLabel;
    @FXML private Label trendPublicationsLabel;
    @FXML private VBox topPerformingContainer;

    // Store previous counts for trend calculation
    private int previousTotalPosts = 0;
    private int previousApprovedPosts = 0;
    private int previousPendingPosts = 0;

    // Color palette for charts
    private String[] colorPalette = {
            "#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6",
            "#1abc9c", "#d35400", "#c0392b", "#16a085", "#8e44ad"
    };

    @FXML
    public void initialize() {
        try {
            // Remove these lines that set chart titles since they're already defined in FXML
            // categoriesChart.setTitle("Categories Distribution");
            // statusChart.setTitle("Publication Status");
            // monthlyPostsChart.setTitle("Monthly Publication Trends");
            // engagementChart.setTitle("Engagement by Category");

            // Add labels to the axes
            monthlyPostsChart.getXAxis().setLabel("Year-Month");
            monthlyPostsChart.getYAxis().setLabel("Number of Publications");
            engagementChart.getXAxis().setLabel("Category");
            engagementChart.getYAxis().setLabel("Engagement Count");

            // Configure chart settings
            categoriesChart.setLegendVisible(true);
            statusChart.setLegendVisible(true);
            monthlyPostsChart.setLegendVisible(true);
            engagementChart.setLegendVisible(true);

            // Ensure axis labels are visible
            ((CategoryAxis) monthlyPostsChart.getXAxis()).setTickLabelRotation(45);
            ((NumberAxis) monthlyPostsChart.getYAxis()).setForceZeroInRange(false);
            ((NumberAxis) monthlyPostsChart.getYAxis()).setTickUnit(1);

            loadStatistics();
        } catch (Exception e) {
            System.err.println("Error in initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshStatistics() {
        try {
            previousTotalPosts = Integer.parseInt(totalPostsLabel.getText());
            previousApprovedPosts = Integer.parseInt(approvedPostsLabel.getText());
            previousPendingPosts = Integer.parseInt(pendingPostsLabel.getText());
        } catch (NumberFormatException e) {
            // Ignore if parsing fails
        }
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            System.out.println("Starting to load statistics...");

            // Clear existing chart data to prevent duplicates
            categoriesChart.getData().clear();
            statusChart.getData().clear();
            monthlyPostsChart.getData().clear();
            engagementChart.getData().clear();

            // Clear dynamic nodes to prevent duplication
            if (monthlyPostsChart.getParent() instanceof Pane) {
                ((Pane) monthlyPostsChart.getParent()).getChildren()
                        .removeIf(node -> node instanceof VBox && node != monthlyPostsChart);
            }
            if (engagementChart.getParent() instanceof Pane) {
                ((Pane) engagementChart.getParent()).getChildren()
                        .removeIf(node -> node instanceof Label && node != engagementChart);
            }

            // Set default values
            totalPostsLabel.setText("0");
            approvedPostsLabel.setText("0");
            pendingPostsLabel.setText("0");
            mostActiveCategoryLabel.setText("N/A");

            List<Post> allPosts = PostDAO.findAll();
            System.out.println("Found " + allPosts.size() + " posts");

            if (allPosts.isEmpty()) {
                System.out.println("No posts found, using default values");
                Label noDataLabel = new Label("No publication data available");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
                if (monthlyPostsChart.getParent() instanceof Pane) {
                    ((Pane) monthlyPostsChart.getParent()).getChildren().add(noDataLabel);
                }
                return;
            }

            // Filter out posts with invalid publishDate
            allPosts = allPosts.stream()
                    .filter(post -> post.getPublishDate() != null)
                    .collect(Collectors.toList());

            // Basic statistics - count each status type explicitly
            int approvedCount = 0;
            int pendingCount = 0;
            
            for (Post post : allPosts) {
                if ("approved".equals(post.getStatus())) {
                    approvedCount++;
                } else if ("pending".equals(post.getStatus())) {
                    pendingCount++;
                }
            }
            
            // Total should be the sum of approved and pending
            int totalPosts = approvedCount + pendingCount;
            
            // Debug output to verify counts
            System.out.println("Total posts: " + totalPosts);
            System.out.println("Approved posts: " + approvedCount);
            System.out.println("Pending posts: " + pendingCount);

            // Update labels with Platform.runLater to ensure UI updates
            int finalApprovedCount = approvedCount;
            int finalPendingCount = pendingCount;
            int finalTotalPosts = totalPosts;
            Platform.runLater(() -> {
                totalPostsLabel.setText(String.valueOf(finalTotalPosts));
                approvedPostsLabel.setText(String.valueOf(finalApprovedCount));
                pendingPostsLabel.setText(String.valueOf(finalPendingCount));
                
                System.out.println("UI Labels updated:");
                System.out.println("Total posts label: " + totalPostsLabel.getText());
                System.out.println("Approved posts label: " + approvedPostsLabel.getText());
                System.out.println("Pending posts label: " + pendingPostsLabel.getText());
            });
            
            // Make sure the pending posts label is visible
            if (pendingPostsLabel != null) {
                pendingPostsLabel.setVisible(true);
                pendingPostsLabel.setOpacity(1.0);
                System.out.println("Pending posts label set to: " + pendingCount);
            } else {
                System.err.println("pendingPostsLabel is null!");
            }
            
            // Force UI update
            if (totalPostsLabel.getParent() != null) {
                totalPostsLabel.getParent().layout();
            }
            
            // Calculate trends
            calculateTrend(totalPostsTrend, totalPosts, previousTotalPosts);
            calculateTrend(approvedPostsTrend, approvedCount, previousApprovedPosts);
            calculateTrend(pendingPostsTrend, pendingCount, previousPendingPosts);

            // Process categories
            Map<String, Long> categoryCounts = allPosts.stream()
                    .filter(post -> post.getCategory() != null)
                    .collect(Collectors.groupingBy(Post::getCategory, Collectors.counting()));

            String mostActiveCategory = categoryCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
            mostActiveCategoryLabel.setText(mostActiveCategory);

            // Categories chart
            ObservableList<PieChart.Data> categoryData = FXCollections.observableArrayList();
            int colorIndex = 0;
            for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
                PieChart.Data slice = new PieChart.Data(
                        entry.getKey() + " (" + entry.getValue() + " posts)", entry.getValue());
                categoryData.add(slice);
                colorIndex = (colorIndex + 1) % colorPalette.length;
            }
            categoriesChart.setData(categoryData);
            applyCustomColorsToChart(categoriesChart);

            // Status chart
            ObservableList<PieChart.Data> statusData = FXCollections.observableArrayList();
            statusData.add(new PieChart.Data("Approved (" + approvedCount + " posts)", approvedCount));
            statusData.add(new PieChart.Data("Pending (" + pendingCount + " posts)", pendingCount));
            statusChart.setData(statusData);
            applyCustomColorsToChart(statusChart);

            // Monthly posts chart - Show data for the last 2 years
            int currentYear = LocalDateTime.now().getYear();
            int startYear = currentYear - 1; // Show last 2 years (e.g., 2024 and 2025)
            Map<String, Long> postsByYearMonth = new TreeMap<>();

            // Group posts by year and month
            for (Post post : allPosts) {
                LocalDateTime publishDate = post.getPublishDate();
                int year = publishDate.getYear();
                if (year < startYear || year > currentYear) continue;
                Month month = publishDate.getMonth();
                String yearMonthKey = year + "-" + month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                postsByYearMonth.put(yearMonthKey, postsByYearMonth.getOrDefault(yearMonthKey, 0L) + 1);
            }

            // Create series for the chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Publications");

            // Add data points for each year-month combination
            for (int year = startYear; year <= currentYear; year++) {
                for (Month month : Month.values()) {
                    String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    String yearMonthKey = year + "-" + monthName;
                    long count = postsByYearMonth.getOrDefault(yearMonthKey, 0L);
                    XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(yearMonthKey, count);
                    series.getData().add(dataPoint);
                }
            }

            monthlyPostsChart.getData().clear();
            monthlyPostsChart.getData().add(series);

            // Style the series
            if (series.getNode() != null) {
                series.getNode().setStyle("-fx-stroke: #3498db; -fx-stroke-width: 3px; -fx-fill: #3498db55;");
            }

            // Add tooltips to data points
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    Tooltip tooltip = new Tooltip(data.getXValue() + ": " + data.getYValue() + " publications");
                    Tooltip.install(node, tooltip);
                    // Ensure the symbol is visible
                    node.setStyle("-fx-shape: \"M 0 0 L 10 0 L 10 10 L 0 10 Z\"; -fx-background-color: #3498db;");
                }
            }

            // Engagement chart
            XYChart.Series<String, Number> likesSeries = new XYChart.Series<>();
            likesSeries.setName("Likes");
            XYChart.Series<String, Number> commentsSeries = new XYChart.Series<>();
            commentsSeries.setName("Comments");
            Map<String, Integer> likesPerCategory = new HashMap<>();
            Map<String, Integer> commentsPerCategory = new HashMap<>();
            for (Post post : allPosts) {
                if (!"approved".equals(post.getStatus())) continue;
                String category = post.getCategory();
                int likes = 0;
                int comments = 0;
                try {
                    likes = LikeDAO.countLikes(post.getId());
                    comments = CommentDAO.countCommentsByPostId(post.getId());
                } catch (SQLException e) {
                    System.err.println("Error counting engagement for post " + post.getId() + ": " + e.getMessage());
                }
                likesPerCategory.put(category, likesPerCategory.getOrDefault(category, 0) + likes);
                commentsPerCategory.put(category, commentsPerCategory.getOrDefault(category, 0) + comments);
            }
            for (String category : categoryCounts.keySet()) {
                XYChart.Data<String, Number> likeData = new XYChart.Data<>(category, likesPerCategory.getOrDefault(category, 0));
                likesSeries.getData().add(likeData);
                XYChart.Data<String, Number> commentData = new XYChart.Data<>(category, commentsPerCategory.getOrDefault(category, 0));
                commentsSeries.getData().add(commentData);

                // Add tooltips
                if (likeData.getNode() != null) {
                    Tooltip.install(likeData.getNode(), new Tooltip(category + " Likes: " + likeData.getYValue()));
                }
                if (commentData.getNode() != null) {
                    Tooltip.install(commentData.getNode(), new Tooltip(category + " Comments: " + commentData.getYValue()));
                }
            }
            engagementChart.getData().clear();
            engagementChart.getData().addAll(likesSeries, commentsSeries);

            // Style the series
            if (likesSeries.getNode() != null) {
                likesSeries.getNode().setStyle("-fx-bar-fill: #3498db;");
            }
            if (commentsSeries.getNode() != null) {
                commentsSeries.getNode().setStyle("-fx-bar-fill: #2ecc71;");
            }

            // Add trending and engagement labels
            addTrendingAndEngagementLabels(allPosts, categoryCounts, likesPerCategory, commentsPerCategory);

            // Create top posts cards
            displayTopPosts(allPosts);

        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addTrendingAndEngagementLabels(List<Post> allPosts, Map<String, Long> categoryCounts,
                                                Map<String, Integer> likesPerCategory, Map<String, Integer> commentsPerCategory) {
        // Trending posts for monthlyPostsChart
        VBox trendingBox = new VBox(5);
        List<Post> trendingPosts = allPosts.stream()
                .filter(p -> "approved".equals(p.getStatus()))
                .sorted((p1, p2) -> p2.getPublishDate().compareTo(p1.getPublishDate()))
                .limit(3)
                .collect(Collectors.toList());

        for (int i = 0; i < trendingPosts.size(); i++) {
            Post post = trendingPosts.get(i);
            String content = post.getContent();
            if (content != null && content.length() > 30) {
                content = content.substring(0, 27) + "...";
            }
            HBox postBox = new HBox(8);
            postBox.setAlignment(Pos.CENTER_LEFT);
            Circle categoryIndicator = new Circle(5);
            categoryIndicator.setFill(Color.web(colorPalette[i % colorPalette.length]));
            Label postLabel = new Label(post.getCategory() + ": " + content);
            postLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-font-weight: bold;");
            postBox.getChildren().addAll(categoryIndicator, postLabel);
            trendingBox.getChildren().add(postBox);

            try {
                int likes = LikeDAO.countLikes(post.getId());
                int comments = CommentDAO.countCommentsByPostId(post.getId());
                if (likes > 0 || comments > 0) {
                    Label engagementLabel = new Label("   ❤ " + likes + "  💬 " + comments);
                    engagementLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                    trendingBox.getChildren().add(engagementLabel);
                }
            } catch (SQLException e) {
                System.err.println("Error fetching engagement: " + e.getMessage());
            }
        }

        // Adjust position to ensure visibility
        trendingBox.setLayoutX(20); // Move to top-left corner
        trendingBox.setLayoutY(20);
        trendingBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 10; " +
                "-fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        if (monthlyPostsChart.getParent() instanceof Pane) {
            ((Pane) monthlyPostsChart.getParent()).getChildren().add(trendingBox);
        }

        // Engagement label for engagementChart
        String highestEngagementCategory = "";
        int maxEngagement = 0;
        for (String category : categoryCounts.keySet()) {
            int totalEngagement = likesPerCategory.getOrDefault(category, 0) +
                    commentsPerCategory.getOrDefault(category, 0);
            if (totalEngagement > maxEngagement) {
                maxEngagement = totalEngagement;
                highestEngagementCategory = category;
            }
        }

        Label engagementLabel = new Label("Most Engaged: " + highestEngagementCategory +
                " (" + maxEngagement + " interactions)");
        engagementLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333; " +
                "-fx-background-color: rgba(255,255,255,0.8); -fx-padding: 5 10; -fx-background-radius: 5;");
        engagementLabel.setLayoutX(engagementChart.getWidth() - 200);
        engagementLabel.setLayoutY(20);
        if (engagementChart.getParent() instanceof Pane) {
            ((Pane) engagementChart.getParent()).getChildren().add(engagementLabel);
        }

        // Update engagementCategoryLabel and trendPublicationsLabel
        if (engagementCategoryLabel != null) {
            engagementCategoryLabel.setText(highestEngagementCategory + " (" + maxEngagement + " interactions)");
        }
        if (trendPublicationsLabel != null && !trendingPosts.isEmpty()) {
            String trendText = trendingPosts.stream()
                    .map(Post::getCategory)
                    .collect(Collectors.joining(", "));
            trendPublicationsLabel.setText(trendText);
        } else if (trendPublicationsLabel != null) {
            trendPublicationsLabel.setText("No trending publications");
        }
    }

    private void calculateTrend(Text trendLabel, int currentValue, int previousValue) {
        if (previousValue == 0) {
            trendLabel.setText("");
            return;
        }

        int difference = currentValue - previousValue;
        double percentChange = (difference / (double) previousValue) * 100;
        String formattedPercent = String.format("%.1f%%", Math.abs(percentChange));

        if (difference > 0) {
            trendLabel.setText("↑ " + formattedPercent);
            trendLabel.getStyleClass().remove("stat-trend-down");
            trendLabel.getStyleClass().add("stat-trend-up");
        } else if (difference < 0) {
            trendLabel.setText("↓ " + formattedPercent);
            trendLabel.getStyleClass().remove("stat-trend-up");
            trendLabel.getStyleClass().add("stat-trend-down");
        } else {
            trendLabel.setText("0%");
            trendLabel.getStyleClass().removeAll("stat-trend-up", "stat-trend-down");
        }
    }

    private void applyCustomColorsToChart(PieChart chart) {
        try {
            if (chart == null || chart.getData() == null) {
                System.err.println("Chart or chart data is null");
                return;
            }

            int i = 0;
            for (PieChart.Data data : chart.getData()) {
                if (data != null && data.getNode() != null) {
                    String color = colorPalette[i % colorPalette.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                    i++;
                }
            }
            chart.setLegendVisible(true);
        } catch (Exception e) {
            System.err.println("Error applying colors to chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTopPosts(List<Post> allPosts) {
        if (topPostsContainer == null) return;

        topPostsContainer.getChildren().clear();

        List<PostEngagement> topPosts = new ArrayList<>();

        for (Post post : allPosts) {
            if (!"approved".equals(post.getStatus())) continue;

            int likes = 0;
            int comments = 0;

            try {
                likes = LikeDAO.countLikes(post.getId());
                comments = CommentDAO.countCommentsByPostId(post.getId());
            } catch (SQLException e) {
                System.err.println("Error counting engagement: " + e.getMessage());
            }

            topPosts.add(new PostEngagement(post, likes, comments));
        }

        topPosts.sort((a, b) -> Integer.compare(
                b.getLikes() + b.getComments(),
                a.getLikes() + a.getComments()));

        int limit = Math.min(5, topPosts.size());

        if (limit == 0) {
            Label noPostsLabel = new Label("No posts with engagement found");
            noPostsLabel.getStyleClass().add("no-data-message");
            topPostsContainer.getChildren().add(noPostsLabel);
            return;
        }

        Label headerLabel = new Label("Top Performing Publications");
        headerLabel.getStyleClass().add("section-header");
        topPostsContainer.getChildren().add(headerLabel);

        for (int i = 0; i < limit; i++) {
            PostEngagement postData = topPosts.get(i);
            VBox card = createPostCard(postData, i);
            topPostsContainer.getChildren().add(card);
        }
    }

    private VBox createPostCard(PostEngagement postData, int rank) {
        Post post = postData.getPost();

        VBox card = new VBox(10);
        card.getStyleClass().add("post-card");
        card.setPrefWidth(220);
        card.setPadding(new Insets(15));

        Label rankLabel = new Label("#" + (rank + 1));
        rankLabel.getStyleClass().add("rank-badge");

        Label categoryLabel = new Label(post.getCategory());
        categoryLabel.getStyleClass().add("post-category");

        String content = post.getContent();
        if (content != null && content.length() > 80) {
            content = content.substring(0, 77) + "...";
        }
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("post-content");
        contentLabel.setWrapText(true);

        String authorName = post.isAnonymous() ? "Anonymous" :
                (post.getUser() != null ? post.getUser().getNom() + " " + post.getUser().getPrenom() : "Unknown");
        Label authorLabel = new Label("By " + authorName);
        authorLabel.getStyleClass().add("post-author");

        HBox metricsBox = new HBox(15);
        metricsBox.setAlignment(Pos.CENTER_LEFT);

        Label likesLabel = new Label("❤ " + postData.getLikes());
        likesLabel.getStyleClass().add("engagement-metric");

        Label commentsLabel = new Label("💬 " + postData.getComments());
        commentsLabel.getStyleClass().add("engagement-metric");

        metricsBox.getChildren().addAll(likesLabel, commentsLabel);

        card.getChildren().addAll(rankLabel, categoryLabel, contentLabel, authorLabel, metricsBox);

        return card;
    }

    // Helper class for post engagement data
    private static class PostEngagement {
        private final Post post;
        private final int likes;
        private final int comments;

        public PostEngagement(Post post, int likes, int comments) {
            this.post = post;
            this.likes = likes;
            this.comments = comments;
        }

        public Post getPost() { return post; }
        public int getLikes() { return likes; }
        public int getComments() { return comments; }
    }
}