#include "MicroBit.h"
#include "aes.hpp"

MicroBit uBit;
uint8_t key[16] = "Jeremy";

/**
 * Déchiffre le texte passé en paramètre (le texte doit être d'une taille de 240 octets)
 */
void decrypt(uint8_t *encrypted_text)
{
    struct AES_ctx ctx;
    AES_init_ctx(&ctx, key);
    for (int i = 0; i < 15; ++i)
    {
        AES_ECB_decrypt(&ctx, encrypted_text + (i * 16));
    }
}

/**
 * Fonction appelé lors de la réception de detagram via radio.
 */
void onRadioReceive(MicroBitEvent)
{
    // Réception du datagram
    PacketBuffer pb = uBit.radio.datagram.recv();

    // Déclaration de la chaine de charactère et copie des informations reçus.
    uint8_t *myjson = (uint8_t *)malloc(240 * sizeof(uint8_t));
    memcpy(myjson, pb.getBytes(), 240);

    // Déchiffrement du texte
    decrypt(myjson);

    // Envoi du texte déchiffré en serial à la passerelle.
    uBit.serial.send(((char *)myjson));
    free(myjson);
}

int main()
{
    // Initialise the micro:bit runtime.
    uBit.init();
    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onRadioReceive);
    uBit.radio.enable();
    uBit.radio.setGroup(83);
    uBit.serial.setRxBufferSize(240);

    // Initialisation de l'outil de chiffrement et du buffer utilisé pour la réception de message via UART.
    struct AES_ctx ctx;
    AES_init_ctx(&ctx, key);
    uint8_t data[240] = "";
    memset(data, 0, sizeof(data));

    while (true)
    {
        if (uBit.serial.rxBufferedSize() > 0)
        {
            // Réception du message passé via UART et copie dans une châine de charactère.
            ManagedString s = uBit.serial.readUntil(";");
            strcat((char *)data, (char *)s.toCharArray());

            // Chiffre puis envoie le code reçu
            for (int i = 0; i < 15; ++i)
            {
                AES_ECB_encrypt(&ctx, data + (i * 16));
            }
            uBit.radio.datagram.send((char *)data);

            // Réinitialisation du buffer
            memset(data, 0, sizeof(data));
        }

        uBit.sleep(50);
    }

    release_fiber();
}
