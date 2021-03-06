proThreaded README:

This is a Maven project. The running of this application requires java 8.  Import this project into intellij or some other
IDE to easily view the code and run the MainApp class to begin the program.

A few points on this app:

1.  The first number represents a counter for the number of downloads you have done for a given file type.
So, 1_0001  represents the #1 file returned on the first search.  2_0012 is the second search, 12th most relevant file.

2. If you the program is unable to download a certain file, then you will see a nonlinear relevant file count.  So, don't
be surprised if files are listed 1_0001,  1_0005, 1_0007, ect ect.  This is the intended functionality.

3. You can query multiple file types.  Feel free select as many checkboxes as you wish.

4. The file path is dynamic.  You will save your files to unique folders located in c:/path/selectedDir/{searchText}

5. The CSS is located in the styles folder under styles.css

6. Pay attention to what each text field is asking for. There are negated words, words you want and exact phrases.  The
negated words will ensure the words are not in the title that you're downloading.  It filters whats coming to your
observableList.

7. The negated words run through a final check where that word cannot appear in the title of your results. This was a
late addition but seems to be working well