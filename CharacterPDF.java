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

	/** Estimated characters per special ability line. */
	static final int SPECIAL_LINE_LENGTH = 30;

	/** Maximum special ability lines on the sheet. */
	static final int NUM_SPECIAL_LINES = 9;

	/** Maximum attack mode lines on the sheet. */
	static final int NUM_ATTACK_LINES = 3;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Number of special ability lines used. */
	int specialLinesUsed;

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
	 			form.getField(ability.name()).setValue(score + "");
				form.getField(ability.getAbbreviation() + "Mod")
					.setValue(Dice.formatBonus(bonus));
			}
			
			// Statistics
			form.getField("ArmorClass").setValue(c.getArmorClass() + "");
			form.getField("HitPoints").setValue(c.getHitPoints() + "");
			form.getField("MoveRate").setValue(c.getMoveInches() + "");

			// Special abilities
			addStringToSpecial(form, getRacialAbilities(c.getRace()));
			addSegmentToSpecial(form, "Feats: ", c.featString());
			addSegmentToSpecial(form, "Skills: ", c.skillString());
			addSegmentToSpecial(form, "Spells: ", c.spellString());

			// Attacks
			int atkNum = 0;
			c.drawWeapon(null);
			while (atkNum < NUM_ATTACK_LINES) {
				c.drawNextWeapon();
				if (c.getWeapon() == null) break;			
				atkNum++;
				Attack atk = c.getAttack();
				form.getField("Weapon" + atkNum).setValue(atk.getName());
				form.getField("AtkBonus" + atkNum).setValue(Dice.formatBonus(atk.getBonus()));
				form.getField("Damage" + atkNum).setValue(atk.getDamage().toString());
				if (c.getWeapon().getMagicBonus() > 0) {
					form.getField("AtkNotes" + atkNum).setValue("Magic");
				}
			}
			
			// Equipment
			for (int i = 0; i < c.getEquipmentCount(); i++) {
				String numStr = formatLeadZeroes(i + 1, 2);
				form.getField("Item" + numStr).setValue(c.getEquipment(i).toString());
				String weightStr = String.format("%.1f", c.getEquipment(i).getWeight());
				form.getField("Weight" + numStr).setValue(weightStr);
			}

			// Encumbrance
			String weightStr = String.format("%.1f", c.getEncumbrance());
			form.getField("WeightTotal").setValue(weightStr);

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
	*  Get racial ability descriptions.
	*/
	String getRacialAbilities (String race) {
		if (race.equals("Dwarf")) {
			return "Infravision 60', resist magic +4, dodge giants +4, find stone traps +1";
		}	
		else if (race.equals("Elf")) {
			return "Multi-classed, infravision 60', hide in woods (4/6), find wood traps +1";
		}
		else if (race.equals("Halfling")) {
			return "Hide in woods (4-in-6), resist magic +4, ranged attacks +4";
		}
		else {
			return "";
		}
	}

	/**
	*  Add a segment to the special ability lines, if nonempty.
	*/
	void addSegmentToSpecial (PDAcroForm form, String label, String segment) throws IOException {
		if (!segment.isEmpty()) {
			addStringToSpecial(form, label + Character.toSentenceCase(segment));		
		}	
	}

	/**
	*  Add a string to the special ability lines.
	*/
	void addStringToSpecial (PDAcroForm form, String s) throws IOException {
		if (!s.isEmpty()) {
			String currLine = "";
			String[] words = s.split(" ");	
			for (String word: words) {
				if (currLine.length() + word.length() + 1 < SPECIAL_LINE_LENGTH) {
					currLine += word + " ";
				}
				else {
					addLineToSpecial(form, currLine);
					currLine = "  " + word + " ";
				}
			}	
			if (!currLine.isEmpty()) {
				addLineToSpecial(form, currLine);
			}
		}
	}

	/**
	*  Add one pre-formatted lins to the special ability section.
	*/
	void addLineToSpecial (PDAcroForm form, String s) throws IOException {
		if (!s.isEmpty()) {
			specialLinesUsed++;
			if (specialLinesUsed <= NUM_SPECIAL_LINES) {
				form.getField("Special" + specialLinesUsed).setValue(s);
			}
		}
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

