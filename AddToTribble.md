# Adding Components Generation to tribble

The following commands add the classes necessary for also generating the components to [tribble](https://github.com/havrikov/tribble/tree/7797acd8801e48cbedb86485032f577cee8ea94c):

'''
cp -r /home/url-fuzzing/tribble-additions/componentExtraction /home/tribble/src/main/scala/saarland/cispa/se/tribble/execution/componentExtraction
cp /home/url-fuzzing/tribble-additions/allRepresentations/* /home/tribble/src/main/scala/saarland/cispa/se/tribble/execution '''

*note*: possible configurations: **allRepresentations** creates plain URLs as well as Firefox and Chromium component representations, **onlyChromiumComponents**: creates only Chromium component representations, **onlyFirefoxComponents**: creates only Firefox component representations, **onlyPlain**: creates only URLs without component representations
'''
cd /home/to/tribble
./gradlew build 
mv ./build/libs/tribble-0.1.jar tribble.jar'''

Execution with a corresponding grammar:
'''java -jar tribble.jar generate --mode=2-path-30 --suffix=.md --grammar-file=/path/to/url-fuzzing/grammars/livingstandard-url.scala --out-dir=/path/to/URLTestFilesRaw'''

The given output directory then contains directories named according to the representations they contain, when generating inputs for all currently available representations these are **plain**, **firefox**, and **chromium**, each of these folders then contains the actual input files.
*note*: the generated input files require some more processing (placing the inputs in test files, registering tests in the test frameworks) before the experiments can be executed, these steps are handled by python scripts in the respective subdirectory of url-fuzzing 