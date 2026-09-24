package dev.qrtext.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import dev.qrtext.repository.CaptureRepository;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class QrService {
  private final CaptureRepository captures;
  public QrService(CaptureRepository captures){this.captures=captures;}
  public String decode(InputStream input) throws IOException, NotFoundException { BufferedImage image=ImageIO.read(input); if(image==null) throw new IOException("Formato de imagem inválido"); return new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)))).getText(); }
  public CaptureRepository captures(){return captures;}
}
