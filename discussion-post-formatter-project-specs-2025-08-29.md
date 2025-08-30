**Discussion Post Formatter Project Preliminary Specifications**

## Context

I am a degree-seeking student registered in the University of the People
(uop) CS 1102 Programming 1 Computer Science course. I am building this
program in increase productivity by automating the formatting of an HTML
snippet to be pasted into University of the People\'s Discussion Forums.
The forum software does not allow classes nor style tags so all CSS must
be applied through the inline style attribute in the HTML tags. Code
highlighting must be done using inline span tags. Any inline code
include inline with plain words should be in span tags with silver
background and a monospaced font two pixels smaller than the plain
wording that surrounds it.

## Coursework

For very unit (one per week, week 1 is unit 1) I must write two
programs:

-   one programming assignment to turn it to the instructor for grading,

-   one program to present to my classmates in the course discussion
    forums in the form of a post that responds to the instructors
    starting prompt which contains wording that specifies what my code
    is expected to do. I think of this as the Assignment Text.

## Assignments

Both assignments have their own unique assignment text with different
requirements.

Both assignments require me to:


1.  - [ ] Write some code in accordance with their respective assignment
    texts.

2.  - [ ] Explain what my code does and how (there may be multiple explanation
    blocks to include in relavent locations in either assignment).

3.  ### Required Inclusions

```{=html}
<!-- -->
```
a.  a copy of the relavent assignment text. Assignments may have more
    than one part or segment.

b.  explanation blocks interspursed through out the post. If a block
    should be preceded or followed by an explanation, a text block
    should be inserted with a short note for the suggestion.

c.  copies of my code files that have been theme-highlighted for that
    assignment or segment.

d.  whatever messaging and/or output is generated in compiling and
    executing my code. If there are empty output values, they should
    substituted with a message stating which value was empty and that it
    was empty.

e.  a discussion question relating to the current unit\'s study material
    that is intended to provoke thought for my classmates and stimulate
    the discussion. The question block should be preceded by an
    Assignment Text block in which to place the portion of the
    assignment text that relates to the discussion question.

f.  a reference block containing relavent references formatted in APA 7
    styling. Reference formatting is beyond the scope of this project.
    So, reference tags should be accepted as user input pasted in from
    the console.

## Unit Organization

Each week\'s unit will have its own package in project cs_1102_base:

-   cs_1102_base.Unit_0

-   cs_1102_base.Unit_1

-   cs_1102_base.Unit_2

-   cs_1102_base.Unit_3

-   cs_1102_base.Unit_4

-   cs_1102_base.Unit_5

-   cs_1102_base.Unit_6

-   cs_1102_base.Unit_7

-   cs_1102_base.Unit_8

Unit_0 is the practice package I created for learning how to use the
tools Eclipse / GitHub / GitHub Copilot provides and all that goes with
it, I call it week zero because I\'m working on
disucussion_post_formatter during the break between terms where
## 3. Assignments

Unit_9 is not a unit of assignments because week 9 is reserved for final
exams which (in previous courses) have not required any programming
## 3. Assignments

## Project Structure

In my project explorer (in Eclipse):

-   The assignment_formatter project is intended to compile and execute
    the code in my weekly unit programming assignment packages while
    collecting all of my code files, compiler messages and output
    generated in the process. It should format everything into a plain
    text file, the content from which can be inserted into my
    programming assignment turn in. It is required to be in .docx
    format.

-   The cs_ll02_base project is where I will be creating and storing my
    weekly unit assignments. Both the programming assignment
    (Programming_Assignment.java) and the Discussion_Forum assignment
    (Discussion_Assignment.java) for their respective week will be in
    the cs_1102_base.Unit_x package where x is the value for unit stored
    in config.txt or typed in by the user through the console (replaces
    the value in config.txt).

-   The discussion_post_formatter project is this project. It is
    intended to do the same things as the assignment_formatter project
    except that instead of producing a plain text file, all of the
    results from discussion_post_formatter will be formatted into an
    HTML code snippet suitible for inserting into a UoP Discussion Forum
    thread.

## Expectations for discussion_post_formatter

In this project:

1.  The unit number specified by unit in config.txt should be used as
    the initialization value for the unit variable in the program. The
    user should be prompted to enter the current unit number before
    processing begins. The inputted unit value will then replace the
    value read in from config.txt and when output is complete,
    config.txt should be rewritten to update unit to the inputted value.
    Theme should be treated the same way. If the user inputs an empty
    string (presses enter without typing a value the initialized value
    is used).

2.  The value of unit determines which package (e.g.
    cs_1102_base.Unit\_{UNIT-NUMBER}) to use for the current run where
    {UNIT_NUMBER} gets replaced with the value of unit.

3.  Currently, the code_file_address uses a wildcard to refer to all
    files in the unit package folder, If there is a more effective way,
    it should be implemented instead.

4.  - [ ] All of the code from the files in the indicated package should be
    copied into memory, compiled and executed while collecting messages
    from the compiler (if none, create a message to that effect) and the
    program output (also, if none, a message to say so).

5.  - [ ] Each file should be contained separately for placement into its own
    code block in the output.

6.  - [ ] Each of the code files in the package folder will be hightlighted
    using span tags to apply inline CSS attributes with the colors
    stored in the project\'s themes folder. CSS classes and style tags
    are not allowed.

7.  The initial theme is specified in config.txt. After unit number is
    inputted, a prompt for the user to enter the name of a theme showing
    the names of the themes available in the themes folder. Like the
    unit, the initialized value is stored in config.txt, the user inputs
    a theme name or number from a list, the theme name is updated in
    config.txt and the theme is then loaded into memory for later use in
    highlighting code in the outputted code blocks.

8.  Non-code blocks should have inline highlighting applied to
    recognizable code snippets that are inline with text that is not
    formatted into a code block.

9.  Explanation blocks must be created from either text files specified
    in config.txt or from user input from the console. Both methods
    should be made available. If the user enters an empty string
    (pressing enter without pasting or typing in a value) the content of
    the text file should be used. At least one explanation block should
    be provided by default.

10. - [ ] Program output should be in the form of an HTML snippet suitible for
    insertion into a forum post responding to the instructor\'s
    discussion prompt in UoPeople\'s discussion forums. UoP forums do
    not allow classes or style tags in a post so all styling must be
    done inline.

11. Before the output is stored, HTML Tidy should be applied for
    readability.

12. All text blocks outputted by discussion_post_formatter should be
    contained in this div container where the{programOutput} placeholder
    becomes the generated code:\
    \<!\-- Output Block \--\>\
    \```html
<div style=\"padding: 10px; border: 0 solid black; zoom: 110%;
    width: 600px;\"\>\
    {programOutput}\
    \</div\>

13. The programOutput variable contains the following six basic text
    blocks and may contain multiples of each. The {content} placeholder
    gets replaced with text either read in from file or pasted in
    through the console. If content is pasted in, it is written into the
    corresponding .txt input file replacing any existing content. When
    program execution is complete, prompt the user to determine whether
    to keep the new contents or revert back to the original. If user
    inputs an empty string (presses enter without pasting anything) the
    contents of the file are inserted instead with a console message
    stating the fact. If the file is empty, a console message says so.

```{=html}
<!-- -->
```
1.  Assignment Text block:\
    \```html
<div style=\"margin-top: 1em; width: 598px; border 1px solid
    transparent;\"\>\
    \<p\>{contentHeading}\</p\>\
    \<p style=\"margin: 0; font-size: 16px;\"\>\
    {content}\</p\>\
    \</div\>

2.  Assignment Sample Code block:\
    \```html
<div style=\"margin: 1em 10px 0 10px; width: 578px; font-size:
    10px;\"\>{contentHeading}\</div\>\
    \```html
<div style=\"margin: 0; width: 578px; border 1px solid transparent;
    font-size: 9px; padding-left: 2em; font-style: italic;\"\>\
    \<pre style=\"white-space: pre-wrap;\"\>\<code\>{content}\
    \</code\>\</pre\>\
    \</div\>

3.  Compiler Message block:\
    \```html
<div style=\"margin-top: 1em; width: 598px; border 1px solid
    transparent;\"\>\
    \<p\>contentHeading\</p\>\
    \<p style=\"margin: 0; background-color: #cecece; font-size:
    16px;\"\>\
    {content}\</p\>\
    \</div\>

4.  Assignment Output block:\
    \```html
<div style=\"margin-top: 1em; width: 598px; border 1px solid
    transparent;\"\>\
    \<p\>{contentHeading}\</p\>\
    \<p style=\"margin: 0; background-color: #cecece; font-size:
    16px;\"\>\
    {content}\</p\>\
    \</div\>

5.  Non-code block:\
    \```html
<div style=\"margin-top: 1em; width: 598px; border 1px solid
    transparent;\"\>\
    \<p\>{contentHeading}\</p\>\
    \<p style=\"margin: 0; font-size: 16px;\"\>\
    {content}\</p\>\
    \</div\>

6.  Question block:\
    \```html
<div style=\"margin-top: 1em; width: 598px; border 1px solid
    transparent;\"\>\
    \<p\>{contentHeading}\</p\>\
    \<p style=\"margin: 0; font-size: 16px;\"\>\
    {content}\</p\>\
    \</div\>

7.  References block:\
    \<div class=\"assignment_text\" style=\"margin-top: 1em;
    margin-left: 0; width: 598px; font: italic 12px/1.5 \'Times New
    Roman\', Georgia, sans-serif;\"\>\
    \<p\>{contentHeading}\</p\>\
    \</div\>\
    \```html
<div style=\"margin: 0; width: 578px; border 1px solid transparent;
    font-size: 12px;\"\>\
    {content}\
    \</div\>

```{=html}
<!-- -->
```
14. Outputted program code is to be highlighted using span tags and the
    colors specified in the themes stored in the folder named /themes/
    in this project\'s folder.
    (F:\\UoPeople\\discussion_post_formatter\\themes). A default theme
    is named in config.txt. The user should be prompted to specify a
    theme in the console. If none is entered (empty string/pressing
    enter) the default theme is used.

15. UoP forums have a display quirk that eats space between blocks. My
    solution has been to attach a title block as shown above to the top
    left of each of the six basic blocks in the outputted HTML.

16. Each unit\'s discussion post will include a question block to
    contain the portion of the assignment text that relates to the
    discussion question, followed by an explanation block to contain the
    actual question posed by the user.

17. Finally, a references block is placed at the end of the post. Since
    reference formatting is beyond the scope of this project, references
    should be either read from a references file or accepted from the
    user pasted into the console.