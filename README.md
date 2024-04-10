
# Introduction

This is a LineBot application designed to provide **Final Fantasy XIV (FFXIV)** data for the China server, developed using **Spring Boot**. It currently offers the following functionalities:

- **Weather Forecast**: Retrieves the FFXIV weather forecast for the user-selected region.
- **In-game Time**: Provides the current in-game time of FFXIV.
- **House Listing**: Lists available houses in FFXIV.
  > **Note**: The house listing feature is deprecated due to the lack of an official API from FFXIV for housing data, and the inaccuracy of third-party data.

Credits to [FFXIVWeather](https://github.com/karashiiro/FFXIVWeather) for the weather data handling concept, and to [XIVAPI](https://xivapi.com/) and [FFCafe](https://www.ffcafe.cn/) for game data.

# Prerequisites

1. JDK 11
2. Selenium 4.18.1 (Optional, only the house listing feature needs it, so you can ignore this if not required.)
3. MySQL 8 or another relational database
4. Redis

# Configuring `application.yml.example`

For configuration details, please refer to `application.yml.example`.

- `line.user.channel.token`, `line.user.secret`: Obtain these from the [Line Developer Console](https://developers.line.biz/console/). You will need these to use the LINE Messaging API.
- `line.user.notify.token`: Obtain this from [LINE Notify](https://notify-bot.line.me/doc/en/). You will need this to use LINE Notify for pushing messages.

# Setup Steps

1. Start MySQL 8 and execute `createTable.sql` and `initTable.sql`.
2. Run `WeatherResourceBuilder.java`. This will update the `.json` files under `resources/ffxiv/resourcedata`.
3. Start Redis.
4. Launch Selenium 4.18.1 along with the required version of Chrome.
5. Prepare the `application.yml` according to your needs.
6. Run the Spring Boot application.

# Using Docker

For ease of deployment, you can use Docker as described below:

1. **Create a Docker Network**: This will be used to connect your containers.
2. **Run MySQL and Redis Containers**: Launch these containers and ensure they are on the created network.
3. **Run docker-compose.yml**: Launch Spring boot app and ensure they are on the created network. 