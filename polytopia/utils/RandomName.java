package polytopia.utils;

import java.util.Random;

public class RandomName {
	private static Random rnd;
	private static String[] pool = {
		"Truisea", "Chuby", "Shiychester", "Quving", "Tidence", "Meley", "Cane", "Pline", "Agosland", "Oniomore", 
		"Slesby", "Oproxrora", "Ufoldale", "Wroccaster", "Phocridge", "Qrolis", "Xing", "Brinas", "Isontol", "Ialing", 
		"Nefield", "Klupgate", "Glifield", "Gliesea", "Tuburg", "Fley", "Zadena", "Phine", "Odonrough", "Olischester", 
		"Vlifield", "Slegow", "Ozlaacridge", "Vruunard", "Wrihtin", "Ojence", "Blagos", "Evodon", "Adagate", "Andoshire", 
		"Jaepolis", "Unomby", "Azhitvale", "Glacgan", "Zlowell", "Zury", "Qrerton", "Uclia", "Ensdon", "Agoving", 
		"Flunbridge", "Chihwood", "Hiyding", "Dresa", "Yiginia", "Kord", "Wreka", "Heim", "Ontmont", "Ockginia", 
		"Zlemburg", "Bledence", "Kinard", "Wroron", "Porith", "Leka", "Elurgh", "Idram", "Oliswood", "Osetin", 
		"Srichull", "Vluecaster", "Klefling", "Figate", "Zaemont", "Rosa", "Qoria", "Ogrok", "Athephis", "Oitdiff",
	};

	static {
		rnd = new Random((int)System.currentTimeMillis());
	}

	public static String roll() {
		return pool[rnd.nextInt(pool.length)];
	}
}