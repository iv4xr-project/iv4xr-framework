package nl.uu.cs.aplib.exampleUsages.miniDungeon;

import nl.uu.cs.aplib.exampleUsages.miniDungeon.DungeonApp;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.MiniDungeonConfig;

/**
 * Launch the game MiniDungeon that you can play. Change the configuration in the code
 * to suit your need.
 */
public class DemoPlayMiniDungeon {
	
	public static void main(String[] args) throws Exception {	
			MiniDungeonConfig config = new MiniDungeonConfig() ;
			config.numberOfMonsters = 12 ;
			config.numberOfHealPots = 4 ;
			config.numberOfRagePots = 4 ;
			config.numberOfScrolls = 5 ;
			config.numberOfCorridors = 5 ;		
			config.viewDistance = 4 ;
			config.nuberOfMaze = 6 ;
			
			System.out.println(">>> Configuration:\n" + config) ;
			var app = new DungeonApp(config) ;
			//app.dungeon.showConsoleIO = false ;
			DungeonApp.deploy(app);
	}

}
