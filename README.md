# Goals
Wolfram cellular automata rule 30 could be used for encryption, as described here: 
https://www.wolframscience.com/nks/p601--cryptography-and-cryptanalysis/

This POC try to mess around this algorithm and is a preparation for a future chat application. My main concern actually is about performance: it should be almost invisible to end user, but it's currently taking ages for a little picture (probably my implementation though).


![Demo of performance](art/demo.gif)

A text of 1024 char will take around 733ms for encryption+decryption so that's almost ok for a chat message. The image of 220*315px (20kB) is taking 9seconds (for decrypt + encrypt), this is not reactive enough... (yet)

# Algorithm

We start with a random key (should be generated from SecureRandom), and we compute the rule 30 on this key: 
each new line provides one "random" new bit (on a specific column, as specified by Wolfram itself), and the new bit is used to xor a bit of the initial message.

## Details of implementation
- To not spend too much time in computation, the "workspace" is reduced to 4096 bits
(or else it will expand for a minimum of "size of message to encode"/2, that's not scalable at all).
Due to this restriction, the first and last bit of each line cannot be computed (not enough data) so they will remain at 1.
These 2 bits could unfortunately impact the predictability of the rule 30 mathematically speaking, but in the state of the art and my knowledge,
the algorithm still looks pretty secure.
(To have an idea of how much difficult it is to find the original key from the encryption key (if you can deduce it in some manners),
you can have a look at the picture here https://www.wolframscience.com/nks/p605--cryptography-and-cryptanalysis/).

- As described in the previous link (https://www.wolframscience.com/nks/p605--cryptography-and-cryptanalysis/) use one bit every 2 lines
looks way stronger from a cryptographic point of view, but the performance I currently have are not good enough to be reduced by 2.

- The current implementation consumes the key:
each line generated with the algorithm is used to encrypt one bit of the message, and then it's used to compute the next line,
so instead of keeping the original key, we keep the last line after each compute.
This also means that the encryption/decryption of multiple messages have to be strictly ordered.
As a chat application should allow each part to send messages asynchronously, a key should be generated for each conversation and for each member of the conversation.
So a one-to-one chat should be based on 2 keys, one for each person, used for encryption. The exchange will deliver the decryption key (the other encryption key).
During the key exchange, no message should be transferred (or the key will not be indexed the same on both device.

- Arising from the previous point, the key is consumed by both parts and is the critical secure parts, so we can't send it again to resync on multiple devices.
This means the *naive* implementation is to establish an encrypted communication between 2 physical devices, it couldn't support to log from another account
or to regenerate old messages (or it should recompute everything again, from the beginning, if we keep the original key in memory?)

