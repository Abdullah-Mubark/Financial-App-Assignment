# Project assignment

Build a financial app using Akka,
It’s going to be a fake financial app, where we will be making (virtual) money. The application is composed of a set of components. Quote generator, traders and the auditor.
Each component is responsible for doing its job as the following:
- The quote generator - this is an absolutely unrealistic simulator that generates the quotes for 5 fictional companies. The market data is published on to a Kafka topic. Traders consume those data to know the current price.
- The traders - these are a set of components that receives quotes from the quote generator and decides whether or not to buy or sell a particular share. To make this decision, they rely on their current amount of money💰💰💰
- The audit -We need to keep a list of all our operations . The audit component receives operations from the Traders and address  It then stores theses in a database.
- The traders should have different criteria to buy and sell suggested by you, and you should show what was the best criteria for making money.
