/*
 *
 *  * Copyright © 2014 - 2021 Leipzig University (Database Research Group)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, version 3.
 *  *
 *  * This program is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ats.evo;

import com.webdifftool.client.model.DiffEvolutionMapping;
import com.webdifftool.client.model.changes.Change;
import com.webdifftool.client.model.changes.basic.AddAttribute;
import com.webdifftool.client.model.changes.complex.ChgAttValue;
import org.gomma.diff.model.ActionData;
import com.webdifftool.server.StopWords;

import java.util.*;

public class DiffComputation {


//    public DiffEvolutionMapping computeDiff(OWLOntology first, OWLOntology second) {
//        String prefix = String.valueOf(Math.abs(new Random().nextLong()));
//        //Globals.addPrefix(prefix);
//        DiffExecutor.getSingleton().setupRepository();
//
//        OWLManager owl = new OWLManager();
//
//        System.out.println("Loading ontology versions");
//        owl.parseAndIntegrateChanges(first, second);
//
//
//        owl.setNewConcepts((HashSet<String>) owl.getNewATSConcepts());
//        owl.setNewRelationships(owl.getNewATSRelationships());
//        owl.setNewAttributes(owl.getNewATSAttributes());
////		owl.parseAndIntegrateChanges(owl.getOBOContentFromFile(oldVersion), owl.getOBOContentFromFile(newVersion));
//
//        Map<String, String> conceptNames = owl.conceptNames; //obo文件中没有定义概念（rdfs:label）
//        System.out.println("Loading rules");
//        this.loadConfigForDiffExecutor();
//        System.out.println("Applying rules");
//        DiffExecutor.getSingleton().applyRules();
//        // currentStatus = "Aggregation of changes";
//        System.out.println("Aggregation of changes");
//        DiffExecutor.getSingleton().mergeResultActions();
//        // currentStatus = "Building of final diff result";
//        System.out.println("Building of final diff result");
//        DiffExecutor.getSingleton().retrieveAndStoreHighLevelActions();
//        DiffEvolutionMapping diffResult = new DiffEvolutionMapping(this.getFullDiffMapping(conceptNames), this.getCompactDiffMapping(), this.getBasicDiffMapping());
//        this.computeWordFrequencies(diffResult);
//        diffResult.computeDependenciesForBasicChanges();
//
//        // Set version sizes
//        diffResult.newVersionConceptSize = owl.getNewVersionConceptSize();
//        diffResult.newVersionRelationshipSize = owl.getNewVersionRelationshipSize();
//        diffResult.newVersionAttributeSize = owl.getNewVersionAttributeSize();
//        diffResult.oldVersionConceptSize = owl.getOldVersionConceptSize();
//        diffResult.oldVersionRelationshipSize = owl.getOldVersionRelationshipSize();
//        diffResult.oldVersionAttributeSize = owl.getOldVersionAttributeSize();
//
//        // Clean repository
//        DiffExecutor.getSingleton().destroyRepository();
//
//        // currentStatus = "Sending results";
//        System.out.println("Sending results");
//        return diffResult;
//    }

    public void loadConfigForDiffExecutor() {
        // System.out.println("Call from: "+getThreadLocalRequest().getRemoteHost());
        DiffExecutor.getSingleton().loadChangeActionDesc("rules/ChangeActions.xml"); //载入演化行为
        DiffExecutor.getSingleton().loadRules("rules/Rule_OWL.xml");//载入演化规则
    }

    public HashMap<String, com.webdifftool.client.model.changes.Change> getFullDiffMapping(Map<String, String> acc2Name) {
        HashMap<String, com.webdifftool.client.model.changes.Change> result = new HashMap<String, com.webdifftool.client.model.changes.Change>();
        DiffExecutor diffExec = DiffExecutor.getSingleton();
        for (ActionData change : diffExec.lowLevelActions) {
            List<String[]> changeValues = new Vector<String[]>();
            for (int i = 0; i < change.dataValues.size(); i++) {
                if (change.changeActionDesc.multipleValues.get(i)) {
                    changeValues.add(change.dataValues.get(i).split("#"));
                } else {
                    changeValues.add(new String[] { change.dataValues.get(i) });
                }
            }

            com.webdifftool.client.model.changes.Change tmpChange = this.getChangeForDiffMapping(change.md5Key, change.changeActionDesc.name, changeValues);
            tmpChange.buildAccessionToNameMap(acc2Name);
            tmpChange.mapsTo = diffExec.allRuleMappings.get(change.md5Key);
            result.put(tmpChange.id, tmpChange);
        }

        for (ActionData change : diffExec.highLevelActions) {
            List<String[]> changeValues = new Vector<String[]>();
            for (int i = 0; i < change.dataValues.size(); i++) {
                if (change.changeActionDesc.multipleValues.get(i)) {
                    changeValues.add(change.dataValues.get(i).split("#"));
                } else {
                    changeValues.add(new String[] { change.dataValues.get(i) });
                }
            }

            com.webdifftool.client.model.changes.Change tmpChange = this.getChangeForDiffMapping(change.md5Key, change.changeActionDesc.name, changeValues);
            tmpChange.buildAccessionToNameMap(acc2Name);
            tmpChange.mapsTo = diffExec.allRuleMappings.get(change.md5Key);
            result.put(tmpChange.id, tmpChange);

        }

        return result;
    }

    public void computeWordFrequencies(DiffEvolutionMapping diffResult) {
        // String fullText = diffResult.getFulltextOfCompactDiff();
        // System.out.println(fullText);
        Map<String, Integer> wordFrequencies = new HashMap<String, Integer>();
        List<String> changeTypes = diffResult.getChangeTypesFromCompactMapping();
        StopWords stop = new StopWords();
        // String[] allWords = fullText.split(" ");
        for (com.webdifftool.client.model.changes.Change chg : diffResult.getAllChangesFromBasicMapping()) {
            if (chg.name.equalsIgnoreCase("addA") || chg.equals("delA")) {
                // List<String> wordsOfChange = chg.getAllWords();
                String[] wordsOfChange = chg.values.get(2)[0].split("[ _]");
                for (String word : wordsOfChange) {
                    word = word.replaceAll("\\[", "");
                    word = word.replaceAll("\\]", "");
                    word = word.replaceAll("\\(", "");
                    word = word.replaceAll("\\)", "");
                    word = word.replaceAll("\\.", "");
                    word = word.replaceAll("\\,", "");
                    word = word.replaceAll("\\;", "");
                    word = word.replaceAll("\\_", " ");
                    // word = word.trim();
                    // word = word.toLowerCase();

                    if (!word.contains(":") && !(word.length() <= 2) && !changeTypes.contains(word) && !stop.is(word)) {
                        if (wordFrequencies.containsKey(word)) {
                            wordFrequencies.put(word, wordFrequencies.get(word) + 1);
                        } else {
                            wordFrequencies.put(word, 1);
                        }
                    }
                }
            }
        }

        diffResult.wordFrequencies = wordFrequencies;
        // System.out.println(wordFrequencies);
    }

    private com.webdifftool.client.model.changes.Change getChangeForDiffMapping(String md5Key, String name, List<String[]> changeValues) {
        if (name.equalsIgnoreCase("addA")) {
            return new AddAttribute(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("delA")) {
            return new com.webdifftool.client.model.changes.basic.DelAttribute(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("addR")) {
            return new com.webdifftool.client.model.changes.basic.AddRelationship(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("delR")) {
            return new com.webdifftool.client.model.changes.basic.DelRelationship(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("addC")) {
            return new com.webdifftool.client.model.changes.basic.AddConcept(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("delC")) {
            return new com.webdifftool.client.model.changes.basic.DelConcept(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("mapC")) {
            return new com.webdifftool.client.model.changes.basic.MapConcept(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("addSubGraph")) {
            return new com.webdifftool.client.model.changes.complex.AddSubGraph(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("delSubGraph")) {
            return new com.webdifftool.client.model.changes.complex.DelSubGraph(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("addInner")) {
            return new com.webdifftool.client.model.changes.complex.AddInner(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("addLeaf")) {
            return new com.webdifftool.client.model.changes.complex.AddLeaf(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("delInner")) {
            return new com.webdifftool.client.model.changes.complex.DelInner(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("delLeaf")) {
            return new com.webdifftool.client.model.changes.complex.DelLeaf(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("merge")) {
            return new com.webdifftool.client.model.changes.complex.Merge(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("split")) {
            return new com.webdifftool.client.model.changes.complex.Split(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("substitute")) {
            return new com.webdifftool.client.model.changes.complex.Substitute(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("toObsolete")) {
            return new com.webdifftool.client.model.changes.complex.ToObsolete(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("revokeObsolete")) {
            return new com.webdifftool.client.model.changes.complex.RevokeObsolete(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("chgAttValue")) {
            return new ChgAttValue(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("move")) {
            return new com.webdifftool.client.model.changes.complex.Move(md5Key, name, changeValues);
        } else if (name.equalsIgnoreCase("mapR")) {
            return new com.webdifftool.client.model.changes.basic.MapRelationship(md5Key, name, changeValues);
        }
        return new Change(md5Key, name, changeValues);
    }

    public Map<String, List<String>> getCompactDiffMapping() {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        Set<String> allDependantChanges = new HashSet<String>();
        DiffExecutor diffExec = DiffExecutor.getSingleton();

        for (String from : diffExec.allRuleMappings.keySet()) {
            allDependantChanges.addAll(diffExec.allRuleMappings.get(from));
        }

        for (ActionData change : diffExec.lowLevelActions) {
            if (!allDependantChanges.contains(change.md5Key)) {
                List<String> currentChanges = result.get(change.changeActionDesc.name);
                if (currentChanges == null) {
                    currentChanges = new Vector<String>();
                }
                currentChanges.add(change.md5Key);
                result.put(change.changeActionDesc.name, currentChanges);
            }
        }

        for (ActionData change : diffExec.highLevelActions) {
            if (!allDependantChanges.contains(change.md5Key)) {
                List<String> currentChanges = result.get(change.changeActionDesc.name);
                if (currentChanges == null) {
                    currentChanges = new Vector<String>();
                }
                currentChanges.add(change.md5Key);
                result.put(change.changeActionDesc.name, currentChanges);
            }
        }
        return result;
    }

    public Map<String, List<String>> getBasicDiffMapping() {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        DiffExecutor diffExec = DiffExecutor.getSingleton();

        for (ActionData change : diffExec.lowLevelActions) {
            List<String> currentChanges = result.get(change.changeActionDesc.name);
            if (currentChanges == null) {
                currentChanges = new Vector<String>();
            }
            currentChanges.add(change.md5Key);
            result.put(change.changeActionDesc.name, currentChanges);
        }
        return result;
    }
}
