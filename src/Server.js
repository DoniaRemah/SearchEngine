const express=require('express');
var app=express();
const mongoose=require('mongoose');
const mongo=require('mongodb');
const bodyParser=require('body-parser');
const cors=require('cors');
const lodash=require('lodash');
const fs=require('fs');
// const { EnglishStemmer } = require('snowball-stemmers');
const Snowball= require('snowball-stemmers');
const e = require('express');

const PORT=3000;
const connectioString='mongodb+srv://abouelhadidola:8aWAvyLwc824XSm8@searchengine.uwkyb5b.mongodb.net/?retryWrites=true&w=majority';

app.use(bodyParser.json());
app.use(cors());

//Connect to the database
const client=new mongo.MongoClient(connectioString,{useNewUrlParser:true,useUnifiedTopology:true});
const connectToDatabase=async()=>{
    try{
        await client.connect();
        console.log('Connected to the database successfully');
    }
    catch(error){
        console.log('Error in connecting to the database');
    }
};

connectToDatabase();
//Access the database
const database=client.db("SearchEngine");
//Start the server
const server=app.listen(PORT,()=>console.log(`Server is running on port ${PORT}`));
server.on('error', () => {
    console.log('Server error', error);
});


// Routes

//test route
app.get('/test',async (req,res)=>{
    const name=req.query.name;
    return res.status(200).json({message: `Hello ${name}`});
}
);

//TODO: the suggestions route
// store the previous queries by the user: in the database 
//Collection called suggestions
// with every new query, search the database for the previous queries and return the suggestions
// the frontend will send the request with every new character added to the query
// the backend will search the database for the previous queries that start with the new query

app.get('/suggestion',async (req,res)=>{
    //get the query from the request
    const query=req.query.query;
    //search the database for the previous queries that start with the new query
    //search in the collection called Suggestions
    //return an array of strings as a response
    //finding only the first 8 results as a maximum limit
    const foundQueries=await database.collection("Suggestions").find({query:{$regex:`^${query}`}}).limit(8).toArray();
    // console.log(foundQueries);
    const response=[];
    foundQueries.forEach(object => {
        response.push(object.query);
    });
    //return the response
    try{
        return res.status(200).json(response);
    }
    catch(error){
        console.log('Error in returning the response');
    }

});



// TODO: The searching route
//note that you have to store the query 
app.get('/search',async (req,res)=>{
    
    //start time
    const start=Date.now();

    //the query is the whole string containing the words that the user is searching for
    const query=req.query.query;
    //pagination query parameters
    const page=parseInt(req.query.page);
    const limit=parseInt(req.query.limit) || 10; 

    ///////////////////////////////////////////////////////////////Suggestion addition///////////////////////////////////////////////

    //save the query in the database in collection called "Suggestions"
    //create the collection
    const existingQuery= await database.collection("Suggestions").findOne({query:query});
    if(existingQuery){
        //do nothing 
        console.log('A user searched for the same query before');
    }
    else{
        //save the query in the database
        //create the document
        try{
            //insert the query in the database
            await database.collection("Suggestions").insertOne({query:query});
            console.log('The query is saved in the suggestion database');
        }
        catch(error){
            console.log("Error in saving the query in the database");
        }

    }

    ///////////////////////////////////////////////////////////////Preprocessing on query///////////////////////////////////////////////
    //1- Remove all special characters and numbers using lodash
    const query1=lodash.replace(query, /[^a-zA-Z ]/g, "");

    //2- Remove all the stop words
    //the stop words are stored in a file called StopWords.txt
    //read the file, one word per line
    const stopWords=fs.readFileSync('StopWords.txt','utf8').split('\r\n');
    // console.log(stopWords);
    //remove the stop words from the query
    const query2=lodash.difference(query1.split(' '),stopWords).join(' ');


    //3- lowercase all the words
    const query3=query2.toLowerCase();

    //4- remove the duplicate words
    const query4=lodash.uniq(query3.split(' ')).join(' ');


    //5- split the query into words
    const queryWords=query4.split(' ');

    //6- Stemming
    // const stemmer = new EnglishStemmer();
    const stemmer = Snowball.newStemmer('english');
    const finalQueryWords =queryWords.map(word=>stemmer.stem(word));

    console.log(finalQueryWords);

    ///////////////////////////////////////////////////////////////Search the database///////////////////////////////////////////////
    const foundDocuments=[];
    //for every word in the query, search the database for the documents that contain this word
    //inside the the Indexer collection
    //search for the value in the Word field
    // if the word is found, push the FoundInDocs field array elements to the foundDocuments array
    // access the database
    for(let i=0;i<finalQueryWords.length;i++){
        const foundWord=await database.collection("Indexer").find({Word:finalQueryWords[i]}).toArray();
        // console.log(foundWord);
        if(foundWord.length>0){
            foundWord[0].FoundInDocs.forEach(object => {
                foundDocuments.push(object);
            });    
        }
    }
    // console.log(foundDocuments);

    ///////////////////////////////////////////////////////////////Ranking the documents///////////////////////////////////////////////

    //rank based on the IDF_TF value inside the array of objects
    //the array of objects is the FoundInDocs field
    //the IDF_TF value is the IDF_TF field
    //sort the array
    //the first element is the document with the highest rank
    //the last element is the document with the lowest rank
    //the array is sorted in descending order

    // TODO: tf-idf for more than one word, should I add them?


    ///////////////////////////////////////////////////////////////Return the results///////////////////////////////////////////////
    //initializing the response object
    const response={
        result:[],
        pagination:{},
        time:0
    };
    //pagination
    const startIndex=(page-1)*limit;
    const endIndex=page*limit;
    //slice the array
    const results=foundDocuments.slice(startIndex,endIndex);
    //return the results
    response.result=results;

    const totalResults=foundDocuments.length;
    const totalPages=Math.ceil(totalResults/limit);

    response.pagination.totalResults=totalResults;
    response.pagination.totalPages=totalPages;
    response.pagination.currentPage=page;
    response.pagination.nextPage=page < totalPages ? page + 1 : null,
    response.pagination.previousPage=page > 1 ? page - 1 : null

    //remove the IDF_TF field from all the objects of the response result array
    response.result.forEach(object => {
        delete object.IDF_TF;
    });

    //return the response
    try{
        //end time
        const end=Date.now();
        response.time=parseFloat(((end-start)*0.001).toFixed(3));
        return res.status(200).json(response);
    }
    catch(error){
        console.log('Error in returning the response');
    }

});



