package com.pfe.nova.configuration;
import com.pfe.nova.configuration.AppointmentDAO;
import com.pfe.nova.models.Appointment;

import java.time.LocalDateTime;

public class AppointmentDAOTest {
    public static void main(String[] args) {
        // Create a mock Appointment object
        Appointment appointment = new Appointment();
        appointment.setPatientId(24); // Replace with a valid patient ID from your database
        appointment.setDoctorId(23); // Replace with a valid doctor ID from your database
        appointment.setAppointmentDateTime(LocalDateTime.of(2023, 12, 15, 10, 30)); // Example date and time
        appointment.setStatus("SCHEDULED");

        // Create an instance of AppointmentDAO
        AppointmentDAO appointmentDAO = new AppointmentDAO();

        // Call the createAppointment method and print the result
        boolean result = appointmentDAO.createAppointment(appointment);
        if (result) {
            System.out.println("Appointment created successfully with ID: " + appointment.getId());
        } else {
            System.out.println("Failed to create appointment.");
        }
    }}
