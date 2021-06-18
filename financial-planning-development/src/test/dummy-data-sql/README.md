# Test (dummy) data for insertion into the database 

The data is used for testing purposes.


## Files (sql scripts)

* **admin.sql**
    * Creates a user with the `ADMIN` role. It is the only user who can create a transaction at `/create-transaction` address.
    * Credentials
        * _admin.admin@gmail.com_
        * _Admin_Hell_3_?_21-789+éíá!_

* **dummy-users.sql**
    * Creates a _normal_ users. _Simulation of real user data_.
    * Credentials
        * Password
            * _Password_é_123!_
        * Emails
            * _homer.simpson@gmail.com_
            * _march.simpson@seznam.cz_
            * _lisa.simpson@gmail.com_
            * _bart.simpson@seznam.cz_
            * _maggie.simpson@gmail.com_

    * User with account number **302**, sample transactions are ready for this user, see following bullet.
        * Credentials
            * _mois@example.com_
            * _#Josífek_U_159?_

* **dummy-transactions.sql**
    * Creates sample transactions in _our_ database table _transactions_. Transactions will apply to the user with account number **302**. This account number has the above dummy user with email address **mois@example.com**.
    * Transactions are ready for the month of April (**2020-04-01** - **2020-04-30**).
    * It is also in the database that was provided to us within the school subject _MOIS_.
