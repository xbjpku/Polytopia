package polytopia.graphics;

import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;

import polytopia.gameplay.Tile;
import polytopia.gameplay.TileVariation;

public abstract class Texture {

	/* Static pool of textures. */
	private static HashMap<String, BufferedImage> texturePool;

	static {
		texturePool = new HashMap<String, BufferedImage>();

		Properties paths = new Properties();

		try {
			FileInputStream fstream = new FileInputStream("./resources/textures.XML");
			paths.loadFromXML(fstream);
		} catch(IOException e) {
			System.out.println("IO Error");
		}

		paths.forEach(
			(tileDes, path) -> {
				BufferedImage image = null;
				try {
					image = ImageIO.read(new File((String)path));
				} catch (IOException e) {
					System.out.println(path + " not found");
				}
				texturePool.put((String)tileDes, image);
		});
	}

	public static BufferedImage getTextureByName(String name) {
		return texturePool.get(name);
	}

	public static BufferedImage getTerrainTexture(Tile tile) {
		String terrainDesc = tile.getStyle() == null ? tile.getTerrainType().toString() :
							String.join("-", tile.getTerrainType().toString(), tile.getStyle().toString());
		
		return texturePool.get(terrainDesc);
	}

	public static BufferedImage getVariationTexture(TileVariation variation) {
		String variationDesc = variation.getStyle() == null ? variation.toString() :
							String.join("-", variation.toString(), variation.getStyle().toString());

		return texturePool.get(variationDesc);
	}
}