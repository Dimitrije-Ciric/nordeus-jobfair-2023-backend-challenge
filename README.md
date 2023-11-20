# Backend challenge - solution
## Nordeus - Job Fair 2023

---

*Challenge manual was moved to `challenge-manual.md` file.*

I used `docker` & `docker compose` to solve environment dependency problems. <br/> 
So to start the service, run `docker compose -f compose.dev.yaml up --build -d`, <br/>
and to run unit & integration tests run `docker compose -f compose.test.yaml up --build -d`.

API cURL calls are placed in `api_calls` directory. 
They can be used to test service either by running them as a script or importing in *postman*.

### System & Architectural design decisions

Due to heavy load support requirements, I knew that horizontal scaling was necessary,
so any in-memory solution would be a failure.
In other words, persistent data storing is required for storing auctions, bids, users, ... data
and scheduled job information, to allow the system to execute scheduled jobs after downtime(recovery).

Also, the service must be transactional in order to prevent unhandled concurrency
while users bidding. It can cause many undefined behaviors, especially with user tokens(virtual money).
I know how transaction management works in *postgresql DBMS*, so I picked it up.

*Springs' job scheduler* does not support persistent and clustered mode, that is why I chose *Quartz job scheduler*.

User ID, username, and profile image URL are some of the users' data that are displayed on other users screen,
for example, when someone joins an auction, it sees other users(username, image) that had placed a bid.
In order to prevent coupling with and overloading other microservices for the user data,
I had supposed that the user data is shared between microservices via jwt.

### Service implementation pros & cons

#### Pros

- Service is widely covered with tests because I applied TDD, 
there are also some boundary and learning tests, as it is my first experience with Spring
- Concurrency is handled with DBMS transactions and optimistic locking, 
which prevents for example: a user bids with 15 tokens, 
but it is registered as 16 tokens, 
because some other user placed a bid just before the first one. 
There are also a lot more cases that can occur without handling concurrency properly.
- Some concurrent cases are covered with tests, but they can be described just as stress tests 
because concurrency is nearly impossible to fully test and rely on them
- Implemented job scheduling with Quartz, along with unit & integration tests
- In order to support Open-Closed principle, 
I parameterized the service and placed all service parameters(initial bid value, auction length, ...) 
in `application.properties`

#### Cons

- I expect that implementation is not very pragmatic in terms of framework usage, 
as it is my first experience with Spring
- JWT security is not finished, I implemented only a part for receiving users' data, 
just to be able to test the API
- Quartz is not fully configured and not ready for production
- Joining auction & user notification are not implemented 
as I supposed that there is a separate service for managing user notifications
- Players are not implemented(just raw auction system, without player details), 
I would merge this service with players service, 
in order to remove unnecessary coupling, also the new service will not lose any cohesion.