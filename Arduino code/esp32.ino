// Site ul este http://air-check.local/

#include <WiFi.h>
#include <esp_wifi.h>
#include <Wire.h>
#include "Adafruit_CCS811.h"
#include "Adafruit_BME280.h"
#include <WebServer.h>
#include <ESPmDNS.h>


const char* ssid = "SBLK";
const char* password = "Sebi040603";

const int dustPin = 4;
const int mq4Pin = 34;
unsigned long sampletime_ms = 30000;
unsigned long lowpulseoccupancy = 0;
unsigned long starttime;
float pcs_cm3 = 0;
float co2 = 0;
float temperature = 0;
float humidity = 0;
float mq4Value = 0;
float voltage = 0;
float ug_m3 = 0;

Adafruit_CCS811 ccs;
Adafruit_BME280 bme;
WebServer server(80);

unsigned long lastCO2Time = 0;
unsigned long co2Interval = 30000;

void handleRoot() {
  char page[4096];

  snprintf(page, sizeof(page),
    "<html>\
    <head>\
      <meta http-equiv='refresh' content='10'/>\
      <meta name='viewport' content='width=device-width, initial-scale=1'>\
      <style>\
        body { font-family: Arial; text-align: center; margin-top: 50px; }\
        h1 { color: #333; }\
        p { font-size: 24px; margin: 10px; }\
        .data { font-weight: bold; color: #007BFF; }\
      </style>\
      <title>Air Quality Monitor</title>\
    </head>\
    <body>\
      <h1>Air Quality Monitor</h1>\
      <p>CO2: <span class='data'>%.2f ppm</span></p>\
      <p>Temperature: <span class='data'>%.2f C</span></p>\
      <p>Humidity: <span class='data'>%.2f %%</span></p>\
      <p>Dust: <span class='data'>%.2f ug/m3</span></p>\
      <p>MQ-4 Voltage Data: <span class='data'>%.2f</span></p>\
    </body>\
    </html>",
    co2, temperature, humidity, ug_m3, voltage
  );

  server.send(200, "text/html", page);
}

void setup() {
  Serial.begin(115200);
  delay(2000); 
  
  WiFi.disconnect(true);
  delay(100);
  WiFi.mode(WIFI_STA);
  WiFi.setAutoReconnect(true);
  WiFi.setTxPower(WIFI_POWER_19_5dBm);

  Serial.println("\nConnecting to WiFi...");
  WiFi.begin(ssid, password);

  unsigned long startTime = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - startTime < 20000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected. IP: " + WiFi.localIP().toString());
  } else {
    Serial.println("\nFailed to connect to WiFi. Going to deep sleep.");
    deepSleepRecovery();
  }

  if (!MDNS.begin("air-check")) {
    Serial.println("Error starting mDNS");
    while (1) {
      delay(1000);
    }
  }
  Serial.println("mDNS responder started");

  pinMode(dustPin, INPUT);
  pinMode(mq4Pin, INPUT);
  starttime = millis();

  Serial.println("Starting CCS811...");
  if (!ccs.begin()) {
    Serial.println("Failed to start CCS811 sensor!");
    while (1);
  }

  while (!ccs.available());

  Serial.println("Starting BME280...");
  if (!bme.begin(0x76)) {
    Serial.println("Could not find a valid BME280 sensor!");
    while (1);
  }

  server.on("/", handleRoot);
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  unsigned long duration = pulseIn(dustPin, LOW);
  lowpulseoccupancy += duration;

  if (millis() - starttime > sampletime_ms) {
    float ratio = lowpulseoccupancy / (sampletime_ms * 10.0);
    float concentration = 1.1*pow(ratio,3) - 3.8*pow(ratio,2) + 520*ratio + 0.62;
    pcs_cm3 = concentration / 28300.168;
    ug_m3 = convertToUgM3_PM10(pcs_cm3);

    Serial.print("Dust ug: ");
    Serial.print(ug_m3);
    Serial.println(" µg/m³");

    Serial.print("Dust pcs: ");
    Serial.print(pcs_cm3);
    Serial.println(" pcs/cm³");

    lowpulseoccupancy = 0;
    starttime = millis();
  }

  delay(10);

  if (millis() - lastCO2Time >= co2Interval) {
    if (ccs.available()) {
      if (!ccs.readData()) {
        co2 = ccs.geteCO2();
        Serial.print("CO2: ");
        Serial.print(co2);
        Serial.println(" ppm");

        mq4Value = analogRead(mq4Pin);
        voltage = (mq4Value / 4095.0) * 3.3;

        Serial.print("MQ-4 Voltage Value: ");
        Serial.println(voltage);

        temperature = bme.readTemperature();
        humidity = bme.readHumidity();

        Serial.print("Temperature: ");
        Serial.print(temperature);
        Serial.println(" °C");

        Serial.print("Humidity: ");
        Serial.print(humidity);
        Serial.println(" %");
        
      } else {
        Serial.println("CCS811 read error");
      }
    }
    lastCO2Time = millis();
  }

  server.handleClient();
}

void deepSleepRecovery() {
  Serial.println("Entering deep sleep for 30s...");
  esp_deep_sleep(30 * 1000000);
}

float convertToUgM3_PM10(float pcs_cm3) {
  // Convert pcs/cm³ to µg/m³
  float radius_cm = 5.0e-4;          
  float density_g_cm3 = 1.65;        
  float volume_cm3 = (4.0/3.0) * 3.1416 * pow(radius_cm, 3); 
  float mass_ug = volume_cm3 * density_g_cm3 * 1.0e6;        
  return pcs_cm3 * mass_ug * 1.0e6;  
}
