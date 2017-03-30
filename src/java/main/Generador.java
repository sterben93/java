package main;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import mx.gob.sat.cfd._3.Comprobante;
import mx.gob.sat.cfd._3.ObjectFactory;
import mx.gob.sat.cfd._3.TUbicacionFiscal;
import org.apache.commons.ssl.PKCS8Key;

/**
 *
 * @author Jesus-HP
 */
public class Generador {

    private Comprobante comprobante;
    private Comprobante.Emisor emisor;
    private TUbicacionFiscal ubicacionFiscal;

    public Generador() {
    }

    public void generarComprobante() {
//        try {
        ObjectFactory factory = new ObjectFactory();
        comprobante = factory.createComprobante();

        comprobante.setVersion("3.0");
        comprobante.setSerie("A");
        comprobante.setFolio("1");
        comprobante.setFecha(fecha());
        comprobante.setFormaDePago("Pago en una sola exhibición");
        comprobante.setSubTotal(new BigDecimal(100.00));
        comprobante.setTotal(new BigDecimal(116.00));

        emisor = factory.createComprobanteEmisor();
        emisor.setNombre("IBM");
        emisor.setRfc("AAA010101AAA");

        ubicacionFiscal = factory.createTUbicacionFiscal();
        ubicacionFiscal.setCalle("Temiscoles");
        ubicacionFiscal.setNoExterior("35");
        ubicacionFiscal.setColonia("Nuevo Veracruz");
        ubicacionFiscal.setCodigoPostal("11560");
        ubicacionFiscal.setMunicipio("Veracruz");
        ubicacionFiscal.setEstado("Veracruz");
        ubicacionFiscal.setPais("México");

        comprobante.setTipoDeComprobante(Comprobante.TIPO_INGRESO);
        emisor.setDomicilioFiscal(ubicacionFiscal);
        comprobante.setEmisor(emisor);

        //X509Certificate cert = geX509Certificate(new File("aaa010101aaa_csd_01.cer"));
        //String certificado = getCertificadoBase64(cert);
        //String noCertificado = getNoCertificado(cert);
//            comprobante.setCertificado(certificado);
//            comprobante.setNoCertificado(noCertificado);
//
//            String xml = getXML(document);
//            String xml = "";
//            String cadenaOriginal = generarCadenaOriginal(xml);
//            PrivateKey llavePrivada = getPrivateKEy(
//                    new File("aaaa010101aaa_csd_01.key"),
//                    "a0123456789");
//            String selloDigital = generarSelloDigital(llavePrivada, cadenaOriginal);
//            comprobante.setSello(selloDigital);
        //////////////////////////////////
        try {
            JAXBContext jbContext = JAXBContext.newInstance(Comprobante.class);
            StringWriter sw = new StringWriter();
            Marshaller jaxbMarshaller = jbContext.createMarshaller();
            jaxbMarshaller.marshal(comprobante, sw);
            String xmlString = sw.toString();
            System.out.println(xmlString);
            try {
                saveToFile(xmlString);
            } catch (Exception e1) {
                System.out.println(e1);
            }
//                StringReader reader = new StringReader();
//                Comprobante c = (Comprobante) unmarshaller.unmarshal(reader);

        } catch (JAXBException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
//        } catch (CertificateException ex) {
//            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
//        } catch (IOException ex) {
//            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
//        }
    }

    public static XMLGregorianCalendar fecha() {
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.getInstance();
            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            return date;
        } catch (DatatypeConfigurationException ex) {
            return null;
        }
    }

    public static X509Certificate geX509Certificate(final File certificateFile)
            throws FileNotFoundException, CertificateException, IOException {

        FileInputStream is = null;

        try {
            is = new FileInputStream(certificateFile);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static String getCertificadoBase64(final X509Certificate cert)
            throws CertificateEncodingException {
        return new String(Base64.encode(cert.getEncoded()));
    }

    public static String getNoCertificado(final X509Certificate cert) {
        BigInteger serial = cert.getSerialNumber();
        byte[] sArr = serial.toByteArray();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < sArr.length; i++) {
            buffer.append((char) sArr[i]);
        }

        return buffer.toString();
    }

    private String generarCadenaOriginal(String xml) {
        try {
            StreamSource streamSource = new StreamSource("cadenaoriginal_3_0.xslt");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer xsltTransformer = transformerFactory.newTransformer(streamSource);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            xsltTransformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(output));
            return output.toString();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (TransformerException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return null;
    }

    private PrivateKey getPrivateKEy(final File keyFile, final String password) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(keyFile);
            org.apache.commons.ssl.PKCS8Key pkcs8 = new PKCS8Key(in, password.toCharArray());
            byte[] decrypted = pkcs8.getDecryptedBytes();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decrypted);
            PrivateKey pk = null;
            if (pkcs8.isDSA()) {
                pk = KeyFactory.getInstance("DSA").generatePrivate(spec);
            } else if (pkcs8.isRSA()) {
                pk = KeyFactory.getInstance("RSA").generatePrivate(spec);
            }
            pk = pkcs8.getPrivateKey();
            return pk;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static String generarSelloDigital(final PrivateKey key, final String cadenaOriginal) {
        try {
            Signature sign = Signature.getInstance("SHA1w1thRSA");
            sign.initSign(key);
            sign.update(cadenaOriginal.getBytes());
            byte[] signature = sign.sign();
            return new String(Base64.encode(signature));
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (SignatureException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return null;
    }

    public static void saveToFile(String code) throws FileNotFoundException,
            UnsupportedEncodingException {
        String[] lines = code.split("[\n]+");
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showSaveDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            String url = chooser.getSelectedFile().getAbsolutePath() + ".xsd";
            PrintWriter pw = new PrintWriter(new File(url), "ISO-8859-1");
            for (String line : lines) {
                pw.println(line);
            }
            pw.flush();
            //Message.showInfoMessage("Archivo guardado correctamente");
            JOptionPane.showMessageDialog(null, "Archivo guardado correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
