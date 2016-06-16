# Telegam bot for online Flibusta library
[Русская] (https://github.com/flicus/TempMail/blob/master/russian.md) версия здесь

Allows to browse and download books from [Flibusta] (http://www.flibusta.is) library. Uses [TOR] (https://www.torproject.org) to connect to Flibusta hidden service. Respond only to the bot admin or telegram users which are allowed by the bot admin.

## Installation
First of all ensure there is a tor instance installed and available for use.

## Configuration
Bot checking for configuration parameters in the environment variables first. If there are none of them, searching for bot.ini file with these parameters.

### Parameters
- name    - telegram bot name
- token   - telegram bot token
- torhost - host where tor installed 
- torport - port of the tor
- admin   - telegram username who will be admin for this bot instance 
 
 
