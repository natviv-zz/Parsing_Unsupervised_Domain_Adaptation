
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.ArgUtils;
import edu.stanford.nlp.parser.lexparser.EvaluateTreebank;
import edu.stanford.nlp.parser.lexparser.ExactGrammarCompactor;
import edu.stanford.nlp.parser.lexparser.GrammarCompactor;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Timing;

class CLIParser {

	public static void main(String[] args) {
		
		String treebankPath = null;
		//FileFilter trainFilter = null;
		//FileFilter selftrainFilter = null;
		String selftrainPath = null;
		//FileFilter testFilter = null;
		boolean train = false;
		int argIndex = 0;
		int seedsize = 1000;
		int selftrainsize = Integer.MAX_VALUE;
		String testPath = null;
		List<String> optionArgs = new ArrayList<String>();
	    String encoding = null;
	    
	    Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		op.setOptions("-goodPCFG", "-evals", "tsv");
		op.testOptions.writeOutputFiles = true;
		
		
		
		while (argIndex < args.length && args[argIndex].charAt(0) == '-') {
		      if (args[argIndex].equalsIgnoreCase("-train") ||
		          args[argIndex].equalsIgnoreCase("-trainTreebank")) {
		        train = true;
		        Pair<String, FileFilter> treebankDescription = ArgUtils.getTreebankDescription(args, argIndex, "-test");
		        argIndex = argIndex + ArgUtils.numSubArgs(args, argIndex) + 1;
		        treebankPath = treebankDescription.first();
		       // trainFilter = treebankDescription.second();
		      }
		      		      
		      else if (args[argIndex].equalsIgnoreCase("-seedsize")) {
		          // save the parser to declarative text file
		          seedsize = Integer.parseInt( args[argIndex + 1]);
		          argIndex += 2;
		          System.out.println("Seed size set "+seedsize);
		        }
		      
		      else if (args[argIndex].equalsIgnoreCase("-selftrainsize")) {
		          // save the parser to declarative text file
		          selftrainsize = Integer.parseInt( args[argIndex + 1]);
		          argIndex += 2;
		          System.out.println("Self train size set "+selftrainsize);
		        }
		      
		      else if (args[argIndex].equalsIgnoreCase("-selftraintreebank") ||
		                 args[argIndex].equalsIgnoreCase("-selftrainTreebank") ||
		                 args[argIndex].equalsIgnoreCase("-selftrain")) {
		        Pair<String, FileFilter> treebankDescription = ArgUtils.getTreebankDescription(args, argIndex, "-test");
		        argIndex = argIndex + ArgUtils.numSubArgs(args, argIndex) + 1;
		        selftrainPath = treebankDescription.first();
		     //   selftrainFilter = treebankDescription.second();
		      }
		      
		      else if (args[argIndex].equalsIgnoreCase("-treebank") ||
		                 args[argIndex].equalsIgnoreCase("-testTreebank") ||
		                 args[argIndex].equalsIgnoreCase("-test")) {
		        Pair<String, FileFilter> treebankDescription = ArgUtils.getTreebankDescription(args, argIndex, "-test");
		        argIndex = argIndex + ArgUtils.numSubArgs(args, argIndex) + 1;
		        testPath = treebankDescription.first();
		   //     testFilter = treebankDescription.second();
		      }
		      
		      else {
		          int oldIndex = argIndex;
		          argIndex = op.setOptionOrWarn(args, argIndex);
		          for (int i = oldIndex; i < argIndex; i++) {
		            optionArgs.add(args[i]);
		          }
		      }//end else
		}//end while
		
		 LexicalizedParser lp = null;
		 GrammarCompactor compactor = null;
	     if (op.trainOptions.compactGrammar() == 3) {
	        compactor = new ExactGrammarCompactor(op, false, false);
	      }
	      

	     Treebank trainbank = makeTreebank(treebankPath, op);
	     MemoryTreebank trainTreebank = new MemoryTreebank();
	     System.out.println("original train tree summary "+trainbank.textualSummary());
	     int i = 0;
	     Iterator<Tree> it = trainbank.iterator();
	     while(it.hasNext() && i<seedsize)
	     {
	    	 
	    	 Tree addtree = it.next();
	    //	 System.out.println(addtree.toString());
	    	 trainTreebank.add(addtree);
	    	 i++;
	    	 
	     }
	     
	     trainbank = null;
	     System.gc();
	     
	     
	     System.out.println("train tree summary "+trainTreebank.textualSummary());
	     lp = LexicalizedParser.trainFromTreebank(trainTreebank, op);
	     
	       
	  
	       if (selftrainPath != null) {
	    	      // test parser on treebank
	    	      
	    	      Treebank selftrainTreebank = makeTreebank(selftrainPath, op);
	    	      System.out.println("self train tree summary "+selftrainTreebank.textualSummary());
	    	      LinkedList<Tree> goldTrees = new LinkedList<Tree>();
	    	      System.out.println("self training... "+selftrainPath);
	    	      int count = 0;
	    	      for (Tree goldTree : selftrainTreebank) {
	    	        List<? extends HasWord> sentence = Sentence.toCoreLabelList(goldTree.yieldWords());
	    	        //goldTrees.add(goldTree);
	    	         
	    	         Tree parse = lp.parseTree(sentence);
	    	         //Tree parse = lp.apply(sentence);
	    	         // Results similar in all three cases. Good parses returned.
	    	         //Only handling of null differs.
	    	        
	    	         /*LexicalizedParserQuery parserQuery = lp.lexicalizedParserQuery();
	    	         if(parserQuery.parse(sentence)){
	    	         	Tree bestTree = parserQuery.getBestPCFGParse();
	    	         }*/
	    	         
	    	         trainTreebank.add(parse);
	    	         if (count>=selftrainsize)
	    	        	 {
	    	        	 System.out.println("Reached self train size limit "+count);
	    	        	 break;
	    	        	 }
	    	         
	    	        count++;
	    	        //parse.pennPrint();
	    	      
	    	      }
	    	      
	    	      System.out.println("Memtree size after self train "+trainTreebank.size());
	    	      lp = LexicalizedParser.trainFromTreebank(trainTreebank, op);
	    	     
		
	       }
	       else
	    	   System.out.println("Self training not specified");
	       
	       if(testPath !=null)
	       {
	    	      Treebank testTreebank = makeTreebank(testPath, op);
	    	      System.out.println("test tree summary "+testTreebank.textualSummary());
	   	       
	    		    
	    	      System.out.println("evaluating" + testTreebank.textualSummary());
	    	      EvaluateTreebank evaluator = new EvaluateTreebank(lp);
	    	      evaluator.testOnTreebank(testTreebank);
	    	    
	       }
	       else
	       System.out.println("Test not specified");
		
		      	    
	  } // end main

	private static Treebank makeTreebank(String treebankPath, Options op) {
	    System.err.println("Making treebank from treebank dir: " + treebankPath);
	    Treebank trainTreebank = op.tlpParams.diskTreebank();
	    System.err.print("Reading trees...");
	    trainTreebank.loadPath(treebankPath);
	   

	    Timing.tick("done [read " + trainTreebank.size() + " trees].");
	    return trainTreebank;
	  }
	
	 private static void printOptions(boolean train, Options op) {
		    op.display();
		    if (train) {
		      op.trainOptions.display();
		    } else {
		      op.testOptions.display();
		    }
		    op.tlpParams.display();
		  }
	
  private CLIParser() {} // static methods only
}
