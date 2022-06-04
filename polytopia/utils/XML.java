package polytopia.utils;

import java.util.Properties;
import java.io.*;

public class XML {
	public static void main(String[] args) {

		Properties prop = new Properties();

		prop.setProperty ("field(Xinxi)", "./resources/tiles/Xinxi/field.png");

		try {
			FileOutputStream fstream = new FileOutputStream("textures.XML");
			prop.storeToXML(fstream, "Describes how in-game textures are mapped.");
		} catch (IOException e) {
			System.out.println ("IO Error.");
		}
	}
}