 The interface to the LexicalizedParser has been defined through the CLIParser class. The associated Java file is CLIParser.java.
 To compile the CLIParser class use the following command:
 javac -cp "stanford-parser.jar:" CLIParser.java  
 
 After building the Java file, run it using the following arguments.
 -train : specify the path to directory containing training mrg data file.
 -seedsize : specify the number of sentences in the input data to be used for training the parser. Default size is 1000.  
 -selftrain : specify the path to directory containing self training mrg file. Do not use this argument if no self training is to be done.
 -selftrainsize : specify the number of sentences in the self training data file to be used for retraining the parser. Do not use this argument if no self training 
                  is to be done. By default will take entire directory for self training.
 -test : specify the path to test data mrg file.
 
 An example usage is demonstrated here.
 java -cp "stanford-parser.jar:" CLIParser -evals "tsv" -goodPCFG -train /u/natviv/nlphw3/mrg/brownself.mrg -seedsize 1000 -selftrainsize 1000 
 -selftrain /u/natviv/nlphw3/mrg/wsj.mrg -test /u/natviv/nlphw3/mrg/wsjtest.mrg
 
 Other files in this directory include the report and folder containing the preprocessed data(This was generated using using a mix of 
 cat, sed, wc commands from the terminal. No extra code was written.)
 and some output files. 
