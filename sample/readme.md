# Chappie Sample

## Setup

1. Clone the repository and build the project:  

```
git clone https://github.com/quarkiverse/quarkus-chappie
mvn clean install -DskipTests
```

2. Set up your OpenAI API key:
   You need to set your OpenAI API key as an environment variable or in the `application.properties` file. 

   For example, in your terminal, you can set the environment variable as follows:
   ```
   export QUARKUS_ASSISTANT_OPENAI_API_KEY=your_openai_api_key_here
   ```

   Replace `your_openai_api_key_here` with your actual OpenAI API key.

## Running the Application

To run the application in development mode, use the following command:

`quarkus dev` or `./mvnw quarkus:dev`

Now you can access the application at `http://localhost:8080` and trigger some exceptions.

Click "Get Help" to see the assistant response. Can also use `a` in terminal to trigger the assistant.
