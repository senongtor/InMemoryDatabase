Simple in memory database

This is a very simple in-memory database, which has a very limited command set.

SET [name] [value]: Set a variable [name] to the value [value]. Neither variable names or values will ever contain spaces.

GET [name]: Print out the value stored under the variable [name]. Print NULL if that variable name hasn't been set.

UNSET [name]: Unset the variable [name]

NUMEQUALTO [value]: Return the number of variables equal to [value]. If no values are equal, this should output 0.

END: Exit the program

BEGIN: Open a transactional block

ROLLBACK: Rollback all of the commands from the most recent transaction block. If no transactional block is open, print out INVALID ROLLBACK

COMMIT: Permanently store all of the operations from any presently open transactional blocks

It takes in stdin and print out the command along with the result after your input. It takes one command at a time.