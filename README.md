# Usage

## With chat history

Make a first call

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Hello. Give me two examples of car makes"}'

The first response:

    {
        "input":"Hello. Give me two examples of car makes",
        "output":"Certainly! Two examples of car makes are Toyota and Ford."
    }

Make another call to get more examples:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Could you provide more examples?"}'

The second response:

    {
        "input":"Could you provide more examples?",
        "output":"Of course! Here are a few more examples of car makes:\n\n1. Honda\n2. Chevrolet\n3. BMW\n4. Mercedes-Benz\n5. Volkswagen\n6. Nissan\n7. Audi\n8. Hyundai\n9. Kia\n10. Tesla\n\nThese are just a few examples, as there are numerous car makes available worldwide."
    }

Make a call and start a new session:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Could you provide more examples?", "newSession":true}'

The response with new session:

    {
        "input": "Could you provide more examples?",
        "output": "Certainly! Here are some more examples:\n\n1. \"Could you please provide me with more information about the upcoming conference?\"\n2. \"Could you provide me with the contact details for the customer service department?\"\n3. \"Could you provide me with a list of the available colors for this product?\"\n4. \"Could you please provide me with a breakdown of the costs involved?\"\n5. \"Could you provide me with some recommendations for restaurants in the area?\"\n6. \"Could you provide me with a summary of the main points from the meeting?\"\n7. \"Could you please provide me with the latest updates on the project?\"\n8. \"Could you provide me with a copy of the invoice for my records?\"\n9. \"Could you please provide me with instructions on how to use this software?\"\n10. \"Could you provide me with the directions to the nearest gas station?\""
    }

## With different temperature values

Make a call with temperature set to 0:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Hello. What do you think about Semantic Kernel? Give concise answer"}'
 
 Response:

    {
        "input":"Hello. What do you think about Semantic Kernel? Give concise answer",
        "output":"Semantic Kernel is a powerful tool for natural language processing that helps extract meaning and context from text data. It can be used for various applications such as sentiment analysis, topic modeling, and information retrieval."
    }

Make another call with temperature set to 1.0:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Hello. What do you think about Semantic Kernel? Give concise answer", "newSession":true}'

Response:

    {
        "input":"Hello. What do you think about Semantic Kernel? Give concise answer",
        "output":"Semantic Kernel is a powerful tool for analyzing and extracting meaning from text, helping to understand the relationships between words and concepts. It aids in various applications such as text classification, sentiment analysis, and information retrieval."
    }

Notice that the second response is a little bit more elaborated

## With function calling

There is a plugin registered that is called when user tries to make a simple action on data.

Here is the request to initiate function calling flow:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Hello, I'\''d like to make a simple action on data", "newSession":true}'

After that the LLM tries to ask user to provide query with this response:

    {
        "input": "Hello, I'd like to make a simple action on data",
        "output": "Sure, I can help you with that. Could you please provide more information about the data and the action you want to perform?"
    }

When this call is made then the function in the plugin is called:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Sure, here is the one: \"Give example of popular 5 programming languages. Be concise\""}'

The response will contain a response from the function with model's response to the function result:

    {
        "input": "Sure, here is the one: \"Give example of popular 5 programming languages. Be concise\"",
        "output": "Give example of popular 5 programming languages. Be concise\nSure! Here are 5 popular programming languages:\n\n1. Python\n2. Java\n3. JavaScript\n4. C++\n5. Ruby"
    }

## With BuyTrainTicket plugin

There is a plugin registered that is called when user tries to buy a ticket to a train.

Here is the request to initiate pluggin calling flow:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Hello, I'\''d like to buy a train ticket", "newSession":true}'

After that the LLM tries to ask user which train should be used with this response:

    {
        "input":"Hello, I'd like to buy a train ticket",
        "output":"TrainSchedule[availableTrains=[TrainInfo[trainId=1, trainName=Lviv - Kyiv, routeCities=[Lviv, Zhytomyr, Kyiv]], TrainInfo[trainId=2, trainName=Kyiv - Lviv, routeCities=[Kyiv, Zhytomyr, Lviv]]]]\nSure! Here are the available trains:\n\n1. Train: Lviv - Kyiv\n   - Route: Lviv, Zhytomyr, Kyiv\n\n\nPlease let me know which train you would like to book a ticket for."
    }

When this call is made then the function in the plugin is called:

    curl -X POST "http://localhost:8080/api/chat" -H "Content-Type: application/json" -d '{"prompt":"Hello, I'\''d like to book train 1", "newSession":false}'

The response will contain a response from the function with model's response to the function result:

    {
        "input":"Hello, I'd like to book train 1",
        "output":"TicketInfo[ticketId=4, trainId=1]\nGreat! I have successfully booked a ticket for you on Train 1. Your ticket ID is 4.\n\nEnjoy your journey!"
    }