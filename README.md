# Pixiedust, an esolang by Stephen Leppik

Pixiedust is an esolang so named because I designed it to look like pixie dust. It was created so that, when asked what tells a computer what to do, the answer "fairies and pixie dust" wouldn't be completely wrong.

This language uses the characters `*+.`. Any other non-whitespace characters will result in a syntax error. This is an assembly language with instructions separated by newlines.

## Instructions

* `* O R X Y` is a mathematical operation:
 * `O` specifies the operation to use: `.` for copy, `++` for addition, `+.` for subtraction, `**` for multiplication, `*.` for division, `*+` for modulo. `+*` is reserved for future use.
 * `R` specifies the register to store the result to. Registers hold single 32-bit integers. Each register is two characters, with a few special registers: `**` is the memory pointer, `*.` is the value at the pointer, `*+` reads a byte from STDIN or writes to STDERR, `..` is the test register, and `.*` is the numeric literal portal.
 * `X` and `Y` are expressions. More on these below. For a copy operation, `Y` should be omitted.
* `. C X Y` performs the comparison specified by `C` and stores it with 0/1 in the `..` register. `=<>` are indicated by `*+.`, respectively. `X` and `Y` are expressions.
* `++ X` prints the Unicode character represented by expression `X` to STDOUT.
* `+. L` defines a program label; `L` can be any number of characters.
* `+* T L` jumps to label `L` based on the condition `T`. `T` can be `*` to jump if `..` is not 0, `.` to jump if `..` *is* 0, or `+` to jump regardless of the value in `..`.

Expressions can be either a normal register, or the reserved `.*` register followed by a number literal. A number literal is up to 32 `+` or `.` characters terminated with a `*`. With `+` meaning 1 and `.` meaning 0, they should form the binary representation of the number exactly how it would be stored as an `int` in a Java stack frame. Leading `.` characters can be omitted, and the terminating `*` can be left out if it's at the end of a line.

A *golfed* Hello World program would look like this:

    ++.*+..+...
    ++.*++..+.+
    ++.*++.++..
    ++.*++.++..
    ++.*++.++++
    ++.*+.++..
    ++.*+.....
    ++.*+.+.+++
    ++.*++.++++
    ++.*+++..+.
    ++.*++.++..
    ++.*++..+..
    ++.*+....+
But that's *boring*, and it doesn't even look too much like pixie dust! So I wrote some code to rewrite it to the following, equivalent program:

    +           +               .   *+       ..  +.        ..            .                  .              .          .
    +            +   .*    +   +.    .    +           .+                         *
                       ++.        *   +  +  .  ++     .  .    *                         .              .      .   .       .*
                   +     +.     *  +    + .+         + .   .*                         .                 .   .    . ..
                ++      .  *     ++.    + ++        +  *                 .      .   ..          .              .*
               +     +   .* + .   +     +.              .  .                  .           .   .   .                .
                  +           +  .*+           .   .. .     .*                                 .  .  . ..         . .   .*
    +               +.  *      +. +      .        ++          +*
     +         +. *+                       +       .  ++  ++             ..                                 .     ..
                +  +.        * +  +                 + ..   +  .*                         .
              + +        . *            ++        . + +       ..*            .                          ..
    +                  +  .  *  ++.                  .+.. .          .    .                    . ...        ..
                +       +   .  *+                ..    . .    +*.         .      .       .         .       .*
