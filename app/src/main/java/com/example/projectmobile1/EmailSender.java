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

public class EmailSender extends AsyncTask<Void, Void, Void> {
    private final String senderEmail = "projectmobile085@gmail.com";
    private final String senderPassword = "apnzxbylwuxpjftd"; // A big lack of security, consider using secure methods
    private final String recipientEmail;
    private final String subject;
    private final String messageContent;

    public EmailSender(String recipientEmail, String subject, String messageContent) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.messageContent = messageContent;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Bitmap qrCodeBitmap = generateQRCode(messageContent);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] qrCodeBytes = byteArrayOutputStream.toByteArray();

            Properties mailProperties = new Properties();
            mailProperties.put("mail.smtp.auth", "true");
            mailProperties.put("mail.smtp.starttls.enable", "true");
            mailProperties.put("mail.smtp.host", "smtp.gmail.com");
            mailProperties.put("mail.smtp.port", "587");

            Session mailSession = Session.getInstance(mailProperties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            MimeMessage emailMessage = new MimeMessage(mailSession);
            emailMessage.setFrom(new InternetAddress(senderEmail));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            emailMessage.setSubject(subject);

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(messageContent);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textBodyPart);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setFileName("qr_code.png");
            attachmentPart.setContent(qrCodeBytes, "image/png");
            multipart.addBodyPart(attachmentPart);

            emailMessage.setContent(multipart);

            Transport.send(emailMessage);

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
