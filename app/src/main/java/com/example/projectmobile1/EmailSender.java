package com.example.projectmobile1;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender extends AsyncTask<Void, Void, Bitmap> {
    private final String senderEmail = "projectmobile085@gmail.com";
    private final String senderPassword = "apnzxbylwuxpjftd"; // Big lack of security
    private final String recipientEmail;
    private final String subject;
    private final String messageContent;
    String content;

    public EmailSender(String recipientEmail, String subject, String messageContent) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.messageContent = messageContent;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            // Generate the QR code image
            Bitmap qrCodeBitmap = generateQRCode(messageContent);

            // Convert the QR code image to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] qrCodeBytes = byteArrayOutputStream.toByteArray();

            // Configure email properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(senderEmail, senderPassword);
                        }
                    });

            // Create the email message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create the message body with the QR code image as an attachment
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(messageContent);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Attach the QR code image to the email
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setFileName("qr_code.png");
            attachmentPart.setContent(qrCodeBytes, "image/png");
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Send the email
            Transport.send(message);

            Log.i("EmailSender", "Email sent successfully!");

        } catch (MessagingException e) {
            Log.e("EmailSender", "Failed to send email.", e);
            e.printStackTrace();
        }

        return null;
}
    private Bitmap generateQRCode(String content) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 500, 500);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return qrCodeBitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
