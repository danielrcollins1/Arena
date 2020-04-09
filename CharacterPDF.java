import java.io.File; 
import java.io.IOException; 
import org.apache.pdfbox.pdmodel.PDDocument; 
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

/******************************************************************************
*  Facility for writing PDF character sheets.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2020-04-09
******************************************************************************/

public class CharacterPDF {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of PDF character sheet source file. */
	static final String CHAR_SHEET_FILE = "OED-CharacterSheet.pdf";

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Write a character to a PDF file. 
	*/
	public void writePDF (Character c) {
		try {
			// Open source file
			File file = new File(CHAR_SHEET_FILE);
			PDDocument document = PDDocument.load(file);
			PDAcroForm form = document.getDocumentCatalog().getAcroForm();

			// Identifiers
			form.getField("Character").setValue(c.getName());
			form.getField("RaceClass").setValue(c.getRaceClassDesc());
 			form.getField("Alignment").setValue(c.getAlignment().toString());

			// Abilities
			for (Ability ability: Ability.values()) {
				int score = c.getAbilityScore(ability);
				int bonus = Ability.getBonus(score);
	 			form.getField(Ability.getFullName(ability)).setValue(score + "");
				form.getField(ability + "Mod").setValue(Dice.formatBonus(bonus));
			}
			
			// Statistics
			form.getField("ArmorClass").setValue(c.getArmorClass() + "");
			form.getField("HitPoints").setValue(c.getHitPoints() + "");
			form.getField("MoveRate").setValue(c.getMoveInches() + "");

			// Attacks
			if (c.getAttack() != null) {
				Attack atk = c.getAttack();
				form.getField("Weapon1").setValue(atk.getName());
				form.getField("AtkBonus1").setValue(Dice.formatBonus(atk.getBonus()));
				form.getField("Damage1").setValue(atk.getDamage().toString());
				if (c.getWeapon().getMagicBonus() > 0) {
					form.getField("AtkNotes1").setValue("Magic");
				}
			}

			// Equipment
			for (int i = 0; i < c.getEquipmentCount(); i++) {
				String id = "Item" + formatLeadZeroes(i + 1, 2);
				form.getField(id).setValue(c.getEquipment(i).toString());
			}

			// Write output file
			document.save(c.getFilename() + ".pdf");
			document.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	*  Format number with lead zeroes.
	*/
	String formatLeadZeroes (int value, int places) {
		String s = String.valueOf(value);
		while (s.length() < places) s = "0" + s;
		return s;	
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		CharacterPDF cp = new CharacterPDF();
		Character.setBoostInitialAbilities(true);
		Character c = new Character("Human", "Fighter", 1, null);
		c.setBasicEquipment();
		c.drawBestWeapon(null);
		cp.writePDF(c);
	}
}

