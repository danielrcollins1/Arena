package net.superdan.athena;

import java.util.ArrayList;
import java.util.List;

import static net.superdan.athena.Dice.roll;
import static net.superdan.athena.Spell.Usage.*;

/******************************************************************************
 *  Record of one class gained by a character (XP, level, hit points, etc.).
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2014-05-22
 *  @version 1.0
 ******************************************************************************/

public class ClassRecord {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/**
	 * Hit die for level 0.
	 */
	static final int LEVEL_ZERO_HIT_DIE = 6;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/**
	 * net.superdan.athena.Character reference.
	 */
	Character character;

	/**
	 * Class type for this class.
	 */
	ClassType classType;

	/**
	 * Level in this class.
	 */
	int level;

	/**
	 * Hit points earned for this class.
	 */
	int hitPoints;

	/**
	 * Experience points earned towards this class.
	 */
	int XP;

	/**
	 * Spells known for this class.
	 */
	List<List<Spell>> spellsKnown;

	/**
	 * Feats known for this class.
	 */
	List<Feat> featsKnown;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (by ClassInfo, level, random hit points).
	*/
	public ClassRecord (Character character, ClassType type, int level) {
		this.character = character;
		this.classType = type;
		this.level = level;
		this.XP = type.getXpReq(level);
		rollFullHitPoints();
		addAllFeats();
		addAllSpells();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public ClassType getClassType () { return classType; }
	public int getLevel () { return level; }
	public int getHitPoints () { return hitPoints; }
	public int getXP () { return XP; }

	/**
	*  Return the base attack bonus for this class/level.
	*/
	public int attackBonus () {
		return classType.getAttackBonus(level);
	}

	/**
	*  Add XP to this class.
	*/
	public void addXP (int awardXP) {
		XP += awardXP;
		if (XP >= classType.getXpReqNext(level)) {
			addLevel();
		}
	}

	/**
	*  Increment the level.
	*/
	public void addLevel () {
		level++;	
		XP = Math.max(XP, classType.getXpReq(level));
		addNewHitPoints(level, false);
		if (isFeatLevel(level)) {
			addFeat();
		}
	}

	/**
	*  Decrement the level.
	*/
	public void loseLevel () {

		// At level 0, death/lose class
		if (level < 1) {
			hitPoints = 0;		
			level = -1;
			XP = Integer.MIN_VALUE;
		}

		// Else reduce hit points proportionally
		else {
			if (isFeatLevel(level))
				loseFeat();
			Dice diceInc = classType.getHitDiceInc(level);
			if (diceInc.getAdd() > 0)
				hitPoints -= diceInc.getAdd();
			else
				hitPoints = hitPoints * (level - 1)/level;
			level--;
			XP = Math.min(XP, getMidpointXP());
		}
	}

	/**
	*  Is this a level where we gain a feat?
	*/
	boolean isFeatLevel (int newLevel) {
		return Character.useFeats()
			&& classType.usesFeats()
			&& newLevel % 4 == 0;
	}

	/**
	*  Choose all feats for new character.
	*/
	void addAllFeats() {
		if (Character.useFeats() && classType.usesFeats()) {
			featsKnown = new ArrayList<>();
			for (int i = 1; i <= level; i++) {
				if (isFeatLevel(i)) {
					addFeat();
				}
			}
		}
	}

	/**
	*  Add a random feat to this character.
	*/
	void addFeat () {
		if (featsKnown.size() < Feat.number) {
			Feat newFeat;
			do {
				int rand = roll(Feat.number) - 1;
				newFeat = Feat.values()[rand];
			} while (featsKnown.contains(newFeat));
			featsKnown.add(newFeat);
		}	
	}

	/**
	*  Lose the last feat for this character.
	*/
	void loseFeat () {
		if (featsKnown.size() > 1) {
			featsKnown.remove(featsKnown.size() - 1);
		}	
	}

	/**
	*  Is this feat known by this class record?
	*/
	public boolean hasFeat (Feat f) {
		return featsKnown != null && featsKnown.contains(f);
	}

	/**
	*  String representation of feats known.
	*/
	String featsString() {
		if (featsKnown == null)
			return null;
		else {
			StringBuilder s = new StringBuilder();
			for (Feat feat : featsKnown) {
				if (s.length() > 0)
					s.append(", ");
				s.append(Feat.formatName(feat));
			}
			return s.toString();
		}
	}

	/**
	*  String representation of skills known.
	*/
	String skillsString () {
		if (!classType.usesSkills()) {
			return null;
		}
		else {
			int skillBonus = level + character.getAbilityBonus(Ability.Dex);
			int hearBonus = 1 + level/4 + character.getAbilityBonus(Ability.Int);
			int climbBonus = 1 + level/4 + character.getAbilityBonus(Ability.Dex);
			int backstabMult = level/4 + 2;
			return "Pick, disarm, filch, sneak, hide " + Dice.formatBonus(skillBonus) + "; "
				+ "hear " + Dice.formatBonus(hearBonus) + ", "
				+ "climb " + Dice.formatBonus(climbBonus) + ", "
				+ "backstab " + Dice.formatMultiplier(backstabMult);
		}
	}

	/**
	*  Get midpoint XP for current level.
	*/
	int getMidpointXP () {
		int low = classType.getXpReq(level);
		int high = classType.getXpReqNext(level);
		return (low + high)/2;
	}

	/**
	*  Get adjusted hit dice for this class.
	*/
	public Dice getHitDice () {
		Dice dice = new Dice(classType.getHitDiceTotal(level));
		int conBonus = character.getAbilityBonus(Ability.Con);
		int hpBonus = conBonus * dice.getNum();
		dice.setAdd(dice.getAdd() + hpBonus);
		return dice;
	}

	/**
	*  Roll full hit points from start to current level.
	*/
	void rollFullHitPoints () {
		hitPoints = 0;
		boolean boost = Character.getBoostInitialAbilities();
		for (int newLevel = 0; newLevel <= level; newLevel++) {
			addNewHitPoints(newLevel, boost);
		}
	}

	/**
	*  Add hit points for a given level.
	*  Per OED rules, at initial generation,
	*    we do not accept 1 or 2 hp die-rolls.
	*/
	void addNewHitPoints (int newLevel, boolean initBoost) {
		int hpBonus = character.getAbilityBonus(Ability.Con);

		if (newLevel == 0) {  // Roll d6 for any class
			Dice newDice = new Dice(1, LEVEL_ZERO_HIT_DIE, hpBonus);
			if (initBoost)
				newDice = boostInitHitDice(newDice);
			hitPoints += newDice.boundRoll(1);
		}
		if (newLevel == 1) {  // Pro-rate to new class die
			int revisedRoll = (int) Math.round((hitPoints - hpBonus) 
				* (double) classType.getHitDiceType() / LEVEL_ZERO_HIT_DIE);
			hitPoints = Math.max(1, revisedRoll + hpBonus);
		}
		else if (newLevel > 1) {
			Dice newDice = classType.getHitDiceInc(newLevel);
			if (newDice.getNum() > 0) {  // Roll new die
				newDice.modifyAdd(hpBonus);
				if (initBoost)
					newDice = boostInitHitDice(newDice);
				hitPoints += newDice.boundRoll(1);
			}
			else {  // Add constant for high level
				hitPoints += newDice.getAdd();			
			}
		}		
	}

	/**
	*  Convert hit dice for init boost (no 1's or 2's).
	*/
	private Dice boostInitHitDice (Dice hitDice) {
		Dice boostDice = new Dice(hitDice);	
		boostDice.setSides(boostDice.getSides() - 2);
		boostDice.modifyAdd(boostDice.getNum() * 2);
		return boostDice;	
	}

	/**
	*  Handle a Constitution change to hit points.
	*/
	public void handleConChange (int oldCon) {
		int oldBonus = Ability.getBonus(oldCon);
		int newBonus = character.getAbilityBonus(Ability.Con);
		int diffBonus = newBonus - oldBonus;
		int numDice = classType.getHitDiceTotal(level).getNum();
		hitPoints = Math.max(numDice, hitPoints + diffBonus * numDice);
	}

	/**
	*  Add all expected wizard spells.
	 */
	void addAllSpells() {
		if (classType.usesSpells()) {
			spellsKnown = new ArrayList<>();
			SpellsTable.getInstance(); // instantiate singleton
			SpellsUsable usable = SpellsUsable.getInstance();
			for (int power = 1; power <= usable.getMaxSpellLevel(); power++) {
				spellsKnown.add(new ArrayList<>());
				int spellsUsable = usable.getSpellsUsable(level, power);
				for (int num = 0; num < spellsUsable; num++) {
					addOneSpell(power);
				}
			}
		}
	}

	/**
	*  Add one spell to given level.
	*/
	void addOneSpell (int power) {
		Spell spell;
		SpellsTable table = SpellsTable.getInstance();
		List<Spell> list = spellsKnown.get(power - 1);
		if (list.size() < table.getNumAtLevel(power)) {
			do {
				Spell.Usage usage = rollUsage();
				spell = table.getRandom(power, usage);
			} while (list.contains(spell));
			list.add(spell);
		}
	}

	/**
	 * Roll random spell usage.
	 */
	Spell.Usage rollUsage() {
		return switch (roll(6)) {
			case 1 -> Miscellany;
			case 2, 3 -> Defensive;
			default -> Offensive;
		};
	}

	/**
	*  String representation of spells known.
	*/
	String spellsString() {
		if (spellsKnown == null)
			return null;
		else {
			StringBuilder s = new StringBuilder();
			for (List<Spell> list : spellsKnown) {
				for (Spell spell : list) {
					if (s.length() > 0)
						s.append(", ");
					s.append(spell.getName());
				}
			}
			return s.toString();
		}
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		return classType.getName() 
		 + "(" + level + ", " + hitPoints + ", " + XP + ")";
	}	
}

