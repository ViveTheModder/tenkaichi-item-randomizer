package cmd;
//Tenkaichi Z-Item Randomizer v1.1, by ViveTheModder
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class MainApp
{
	static final String CSV_PATH = "./csv/";
	static final File ITEM_CSV = new File(CSV_PATH+"items.csv");
	static final File ITEM_PURPLE_CSV = new File(CSV_PATH+"items-purple.csv");
	static final File ITEM_RESTRICTION_CSV = new File(CSV_PATH+"items-restrict.csv");
	static final int ABILITY_LIMIT = 7;	//Total Sum of Z-Item Costs
	static final int NUM_SLOTS = 8;		//Max Amount of Z-Items
	static final int NUM_ITEM_B_TYPES = 4;  //Ability Z-Item Categories
	static final int NUM_ITEM_Y_TYPES = 53; //Skill Z-Item Categories
	static final int NUM_ITEMS_PURPLE = 15; //Strategy Z-Item Count
	
	static int itemCnt;
	static int[] costs = new int[NUM_SLOTS];
	static boolean[] categories = new boolean[NUM_ITEM_Y_TYPES+NUM_ITEM_B_TYPES];
	static String[] names = new String[NUM_SLOTS];
	
	public static void setCosts(int itemThreshold)
	{
		for (int i=0; i<ABILITY_LIMIT; i++)
		{
			int randomIndex = (int) (Math.random()*itemThreshold);
			costs[randomIndex]++;
			//BT3 has no 6-point Z-Item - for other ISOs, this fix may not be necessary
			if (costs[randomIndex]==6) costs[randomIndex]--;
		}
	}
	public static void setCategoriesAndNames(int index) throws FileNotFoundException
	{
		int rng = (int) (Math.random()*categories.length); //random Z-Item ID
		Scanner sc = new Scanner(ITEM_CSV);
		while (sc.hasNextLine())
		{
			String input = sc.nextLine();
			String[] inputArray = input.split(",");
			int itemID = Integer.parseInt(inputArray[0]);
			int categoryID = Integer.parseInt(inputArray[1]);
			int itemCost = Integer.parseInt(inputArray[2]);
			String itemName = inputArray[3];
			
			if (itemID==rng)
			{	
				if (costs[index]==itemCost && categories[categoryID]==false)
				{
					categories[categoryID]=true;
					names[index]=itemName; break;
				}
				else //while drunk, I originally used recursion, got a stack overflow, and started laughing a lot
				{    //now that I'm sober, I actually made use of a do-while loop for the first time ever
					do
					{
						input = sc.nextLine();
						inputArray = input.split(",");
						categoryID = Integer.parseInt(inputArray[1]);
						itemCost = Integer.parseInt(inputArray[2]);
						if (categoryID==-1) continue;
					} 
					while (costs[index]!=itemCost); //get closest Z-Item with correct cost
					//obviously repeated instructions for the new Z-Item
					if (categories[categoryID]==false)
					{
						itemName = inputArray[3];
						categories[categoryID]=true;
						names[index]=itemName; break;
					}
				}
			}
		}
		sc.close();
	}
	public static void setRestrictions(int charaHeart) throws FileNotFoundException
	{
		//for both charaHeart and charaRestrict, 0 means pure and 1 means evil
		Scanner sc = new Scanner(ITEM_RESTRICTION_CSV);
		while (sc.hasNextLine())
		{
			String input = sc.nextLine();
			String[] inputArray = input.split(",");
			int categoryID = Integer.parseInt(inputArray[0]);
			int charaRestrict = Integer.parseInt(inputArray[1]);
			if (charaHeart!=charaRestrict || charaHeart>1)
				categories[categoryID]=true;
			else categories[categoryID]=false;
		}
		sc.close();
	}
	public static String getStrategyItemName() throws FileNotFoundException
	{
		int newItemID = (int) (Math.random()*NUM_ITEMS_PURPLE)+137;
		String name="";
		Scanner sc = new Scanner(ITEM_PURPLE_CSV);
		while (sc.hasNextLine())
		{
			String input = sc.nextLine();
			String[] inputArray = input.split(",");
			int itemID = Integer.parseInt(inputArray[0]);
			if (itemID==newItemID) name = inputArray[1];
		}
		sc.close();
		return name;
	}
	public static boolean getRandomBoolean()
	{
		boolean output=false;
		float rng = (float) Math.random();
		if (rng>=0.5) output=true;
		return output;
	}
	public static void setRandomItems(int charaHeart, boolean hasStrategyItem) throws FileNotFoundException
	{
		itemCnt=0;
		int i, itemThreshold = (int) (Math.random()*NUM_SLOTS)+1;
		setRestrictions(charaHeart);
		setCosts(itemThreshold);
		for (i=0; i<NUM_SLOTS; i++)
			if (costs[i]!=0) setCategoriesAndNames(i);
		for (i=0; i<NUM_SLOTS; i++)
			if(names[i]!=null) itemCnt++;
		if (hasStrategyItem==true) itemCnt++;

	}
	public static void main(String[] args) throws FileNotFoundException 
	{
		int charaHeart=-1;
		boolean hasStrategyItem = getRandomBoolean();
		
		if (args.length==1)
		{
			if (args[0].equals("-h") || args[0].equals("-help"))
			{
				System.out.println("Generate a set of random Z-Items based on the character's heart.\n"
				+ "In other words, whether they are good, evil, or even neutral.\nYeah, I know, "
				+ "that last one is not an option for Tenkaichi games, but oh well...\n"
				+ "To decide the character's heart, pass on one of the following arguments:\n"
				+ "-g, -good --> Allow only neutral Z-Items and those meant for good characters\n"
				+ "-e, -evil --> Allow only neutral Z-Items and those meant for bad characters.\n"
				+ "-n, -null --> Allow only neutral Z-Items.");
				System.exit(0);
			}
			else if (args[0].equals("-g") || args[0].equals("-good")) charaHeart=0;
			else if (args[0].equals("-e") || args[0].equals("-evil")) charaHeart=1;
			else if (args[0].equals("-n") || args[0].equals("-null")) charaHeart=2;
			else 
			{
				System.out.println("Invalid argument. Use the -h or -help argument for help.");
				System.exit(1);
			}
		}
		else
		{
			System.out.println("Incorrect number of arguments. Only one argument is required.");
			System.exit(2);
		}
		
		setRandomItems(charaHeart, hasStrategyItem);
		/* for reasons unknown (likely related to the sum of costs not being equal to the Ability Limit, an intentional flaw
		*  on my part), there is a possibility of no Z-Items or only one Strategy Z-Item being generated, and that sucks */
		if (itemCnt==0) setRandomItems(charaHeart, hasStrategyItem);
		if (hasStrategyItem==true && itemCnt<1) setRandomItems(charaHeart, hasStrategyItem);
		System.out.println(String.format("%25s %d]","[Number of Z-Items:",itemCnt));

		for (int i=0; i<NUM_SLOTS; i++)
			if (names[i]!=null) 
				System.out.println(String.format("%-32s %d", names[i], costs[i]));
		if (hasStrategyItem==true) System.out.println(getStrategyItemName());
	}
}
