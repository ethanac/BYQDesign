Files:
1. Sample test files: 
    Correct: "file_correct.txt";
    Incorrect: "file_duplicate_variable_name.txt","file_duplicate_class_name.txt", "file_duplicate_func_name.txt" and "file_func_program.txt".
2. Sample output files: "SymbolTable_correct.txt".
3. State Transition Table: STT_Alpha.csv
4. Code file: all files in the folder "src".

How to run:
1. Copy all text files, STT_Alpha.csv, src folder into the same directory.
2. Open Main.java and run it.
3. Enter 1 or 2 to choose printing symbol tables or writing them to a file.
4. If you choose to write results to a file, the file name is "SymbolTable.txt".
5. If you want to use sample files or your files to test, put it into the directory mentioned above, and change its
   name to "file.txt".

Note:
1. All details of generating symbol tables are shown in the console including error messages.
2. If you choose to output the symbol tables on the screen, it pushes the details of generating symbol tables up.
    Please scroll up to see the information.
3. However, all details of parsing would be written to the file named "ParserOutput.txt". 
    So for the file "file_func_program", it would only show "Parse failed" without show detail error message since the error message is written to the file.