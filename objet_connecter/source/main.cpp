// #define MICROBIT_RADIO_MAX_PACKET_SIZE          1000
// Définition
#include "MicroBit.h"
#include <string.h>
#include "inc/neopixel.h"
#include "MicroBitPin.h"
#include "inc/bme280.h"
#include "inc/tsl256x.h"
#include "inc/veml6070.h"
#include "inc/ssd1306.h"
#include "aes.hpp"
#include "inc/ArduinoJson-v6.21.3.h"

MicroBit uBit;
MicroBitMessageBus bus;
MicroBitI2C i2c(I2C_SDA0,I2C_SCL0);
MicroBitPin P0(MICROBIT_ID_IO_P0, MICROBIT_PIN_P0, PIN_CAPABILITY_DIGITAL_OUT);
ssd1306 screen(&uBit, &i2c, &P0);
bme280 bme(&uBit,&i2c);
tsl256x tsl(&uBit,&i2c);
int swapping = 0;

// fonction pour echanger de ligner la temperature et la luminositer
// il suffit de mettre les arguments dans l'ordre souhaiter
void swapDisplay(ManagedString top, ManagedString bot){
    
    //Nettoyage de l'ecran
    screen.display_line(3,0,"                ");
    screen.display_line(4,0,"                ");
    //affichage sur l'ecran
    screen.display_line(3,0,top.toCharArray());
    screen.display_line(4,0,bot.toCharArray());
    screen.update_screen();
}

// fonction déclencher lors du ubit listen
void onData(MicroBitEvent)
{
    //on recupere le packet
    PacketBuffer pb = uBit.radio.datagram.recv();
    
    // on alloue la memoire pour la donner recuperé
    uint8_t* myjson = (uint8_t*)malloc(240 * sizeof(uint8_t));
    memcpy(myjson, pb.getBytes(), 240);

    // Init du chiffrement, Clé de chiffrement plus structure associé
    uint8_t key[16] =  "Jeremy";
    struct AES_ctx ctx;
    AES_init_ctx(&ctx, key);

    // dechiffrage du paquet
    // AES_ECB_decrypt(&ctx, myjson);
    for (int i = 0; i < 15; ++i)
    {
        AES_ECB_decrypt(&ctx, myjson + (i * 16));
    }

    //converting into json
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, myjson);
    const char* order = doc["data"][0]["value"];
    const char* receiver = doc["receiver"];

    // debugging
    // uBit.serial.puts("\n");
    // uBit.serial.puts(order);
    // uBit.serial.puts(receiver);
    // uBit.serial.puts("\n");

    // Dependant de ce que l'on trouve dans le paquet on modifie le "boolean" d'inversement
    // swapping = 1 -> Luminositer en haut et temperature en bas
    if(strcmp(order,"LT") == 0 && strcmp(receiver,"0") == 0){
        swapping = 1;
    } 
    if(strcmp(order,"TL") == 0 && strcmp(receiver,"0") == 0){
        swapping = 0;
    }

    // libere la memoire du paquet une fois lu
    free(myjson);
}

int main()
{
    // main init valeurs
    uBit.init();
    uBit.radio.enable();
    uBit.radio.setGroup(83);

    // debugging
    // uBit.serial.baud(9600);

    // Init du chiffrement, Clé de chiffrement plus structure associé
    uint8_t key[16] =  "Jeremy";
    struct AES_ctx ctx;
    AES_init_ctx(&ctx, key);

    // Init des valeurs du capteur externe
    uint32_t pressure = 0;
    int32_t temp = 0;
    uint16_t humidite = 0;
    uint16_t comb =0;
    uint16_t ir = 0;
    uint32_t lux = 0;

    // En attente d'un evenement de changement d'interface
    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onData);

    // Affichage basique de l'ecran sans les valeurs du capteurs
    screen.display_line(1,0,"***Affichage***");
    screen.update_screen();

    uint8_t plain_text[240];

    while (true)
    {
        memset(plain_text,0,240);

        // lecture des sensors temperature et luminosité 
        bme.sensor_read(&pressure, &temp, &humidite);
        tsl.sensor_read(&comb, &ir, &lux);
        
        // Attribution des nouvelles valeurs sous formes de managed string
        int tmp = bme.compensate_temperature(temp);
        ManagedString luminosite = ManagedString((int)lux);
        ManagedString temperature = ManagedString(tmp/100) + "." + (tmp > 0 ? ManagedString(tmp%100): ManagedString((-tmp)%100));  

        // Formattage sous forme de protocole Json avec les valeurs des capteurs
        strcat((char*)plain_text,"{\"receiver\" : \"1\",\"emitter\" : \"0\",\"data\" : [{\"type\" : \"T\",\"value\" : \"");
        strcat((char*)plain_text, (char*)temperature.toCharArray());
        strcat((char*)plain_text, "\",\"order\" : \"number\"},{\"type\" : \"L\", \"value\" : \"");
        strcat((char*)plain_text, (char*)luminosite.toCharArray());
        strcat((char*)plain_text, "\", \"order\" : \"number\"}],\"timestamp\" : \"time\"};");

        // Chiffrement du paquet a envoyer par bloc de 16 octets
        // 14 paquets de 16 car plain_text de 224
        for (int i = 0; i < 15; ++i)
        {
            AES_ECB_encrypt(&ctx, plain_text + (i * 16));
        }

        // Verification du boolean global pour savoir si on doit inverser l'affichage
        if(swapping == 1){
            swapDisplay("Lum  : "+luminosite,"Temp : "+temperature);
        } else {
            swapDisplay("Temp : "+temperature,"Lum  : "+luminosite);
        }

        // Envoie des valeurs des capteurs en format JSON chiffré
        uBit.radio.datagram.send((char*)plain_text);

        // Temporisation pour ne pas envoyer les valeurs trop souvent
        uBit.sleep(5000);
    }

    release_fiber();
}
