# Project assignment

Build a financial app using Akka,
It’s going to be a fake financial app, where we will be making (virtual) money. The application is composed of a set of components. Quote generator, traders and the auditor.
Each component is responsible for doing its job as the following:
- The quote generator - this is an absolutely unrealistic simulator that generates the quotes for 5 fictional companies. The market data is published on to a Kafka topic. Traders consume those data to know the current price.
- The traders - these are a set of components that receives quotes from the quote generator and decides whether or not to buy or sell a particular share. To make this decision, they rely on their current amount of money💰💰💰
- The audit -We need to keep a list of all our operations . The audit component receives operations from the Traders and address  It then stores theses in a database.
- The traders should have different criteria to buy and sell suggested by you, and you should show what was the best criteria for making money.

<div style="text-align:center">
    <img alt="assignment" src="images/assignment.jfif" width="485" height="730">
</div>

## Configurations
By default, the app will have the following configurations:
- Quotes generator will generate a new quote every 30 second.
- Quotes generator will generate up to 25 quotes and then terminates app.
- App will have 5 pre-defined companies.
- App will have 3 pre-defined traders.
- Traders will start with initial balance of 10,000 each.
- Traders will have different trading strategies.
- Trader 1 will have a basic trading strategy.
- Trader 2 will have a bad trading strategy.
- Trader 3 will have a good trading strategy.

You can override these configurations if you want

## Installation
You only need to have [docker](https://www.docker.com/products/docker-desktop) installed 

## Usage
Make sure you have [docker](https://www.docker.com/products/docker-desktop) running
- Open a bash terminal
- Run script found at the project root [run_app.sh](run_app.sh)
```bash
./run_app.sh
```
- [run_app.sh](run_app.sh) will start Kafka and MySql clusters and run the app for you
- You should see the following
  ![app_script_running](images/app_script_running.png?raw=true "App Script Running")


- Grab a cup of coffee and enjoy the show ☕

## Additional Info
#### Kafka cluster
- You can get an overview of kafka cluster using Kafdrop through http://localhost:9000/
  ![kafka_cluster_overview](images/kafka_cluster_overview.jpg?raw=true "Kafka Cluster Overview")
#### MySql cluster
- You can get an overview of MySql cluster by running the following commands
```bash
docker exec -it fa_ndb_mgmd_1 bash
ndb_mgm
show
```
  ![mysql_cluster_overview](images/mysql_cluster_overview.jpg?raw=true "MySql Cluster Overview")
- You can access MySql database through localhost:3306 with the following credentials' username = admin, password = admin
## License
[MIT](https://choosealicense.com/licenses/mit/)