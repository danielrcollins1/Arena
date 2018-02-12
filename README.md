ARENA - Java Package for Simulating Original D&D Combat
========================================================

This code package provides routines for simulating combat in
a tabletop Fantasy Role-Playing Game (FRPG) similar to Original D&D
or closely-related games. In most cases, the intent is to output
aggregate statistics based on many trials of the game between men and 
monsters. This package provides only command-line, text output; there 
are no graphics or visualizations, and generally few options for output 
regarding individual combats. 

The package currently includes three top-level main programs:

-----------------------------------------------------------------

ARENA -- Simulates a population of fighters, battling for
experience and treasure over some amount of time (broadly inspired by 
Roman arena-style events). Combat can be man vs. man or man vs.
monster, including simulated excursions against dungeon random 
encounters; individual combats can by one-on-one or in groups. Statistics
at end show level-based demographics of the overall surviving population,
percent of experience from treasure, and number and ratio of deaths
caused by each type monster in the database. 

MONSTER METRICS -- Performs a binary search at each fighter level 1-12
to determine how many fighters represent a matched challenge against
each monster in the database (i.e., closest to a 50/50 victory chance).
Output is the balanced number of fighters at each level, and a suggested
Equivalent Hit Dice (EHD) which can be used for balancing and
experience purposes.

MARSHAL -- Creates random bands of men as encountered in the wilderness
(e.g., bandits, brigands, buccaneers, nomads, etc., in numbers 30-300),
with leaders of appropriate levels, in which the leaders are generated
by simulating their entire combat history in the Arena. 

-----------------------------------------------------------------

GPJ project files for use with jGRASP are included for each of these
three applications.

Note that at the current time, combat is primarily melee only; no sense
of space, location, or movement is simulated. Targets of attacks are 
determined randomly (as per AD&D DMG p. 63 and 70), with each strike. 

While common monster special abilities are modeled, we have not implemented 
magic-user/wizard spells (so: no mixed PC parties with wizards, nor NPC wizard 
opponents). One round of special attacks is allowed for monster abilities
at the start of combat (e.g., dragon breath, giant boulders, medusa gaze, etc.). 

To the extent that the original rules are ambiguous or in need of DM
adjudication (as in many cases), the author has attempted to research 
and interpret the rules such as possible, and uses interpretations
as expressed in the Original Edition Delta (OED) House Rules set. In some
cases, software switches allow toggling between different modes. See
www.oedgames.com for more information. 

Note that the data in the MonsterDatabase file is primarily maintained 
in a spreadsheet external to this package, MonsterDatabase.ods. Changes
to the database should be made there first, and then exported to the CSV 
file in this software package. See www.oedgames.com/addons/houserules.

- Daniel R. Collins, 2018-02-11
delta@superdan.net
www.oedgames.com
