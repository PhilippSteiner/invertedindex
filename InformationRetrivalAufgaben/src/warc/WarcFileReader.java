package warc;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.jsoup.Jsoup;

public class WarcFileReader {

	public static List<String> getHTMLDocumentsAsStrigsFromWarcFileAtPath(String pathToWarcFile) {
		List<String> htmlDocumentsAsStrings = new ArrayList<String>();
		GZIPInputStream gzInputStream;
		try {
			gzInputStream = new GZIPInputStream(new FileInputStream(pathToWarcFile));
			DataInputStream inStream = new DataInputStream(gzInputStream);
			WarcRecord thisWarcRecord;
			while ((thisWarcRecord = WarcRecord.readNextWarcRecord(inStream)) != null) {
				if (thisWarcRecord.getHeaderRecordType().equals("response")) {
					WarcHTMLResponseRecord htmlRecord = new WarcHTMLResponseRecord(
							thisWarcRecord);
					String thisTargetURI = htmlRecord.getTargetURI();
					System.out.println("Loading File from URL : " + thisTargetURI);
					try {
						htmlDocumentsAsStrings.add(Jsoup.connect(htmlRecord.getTargetURI()).get().html());
					} catch (Exception exception) {
						System.out.println("Could not load File, "+ exception.getLocalizedMessage());
					}
				}
			}
			inStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return htmlDocumentsAsStrings;
	}
}