const express=require('express');
var app=express();
const mongoose=require('mongoose');
const bodyParser=require('body-parser');
const cors=require('cors');

const PORT=3000;
const connectioString='mongodb+srv://abouelhadidola:8aWAvyLwc824XSm8@searchengine.uwkyb5b.mongodb.net/?retryWrites=true&w=majority';

app.use(bodyParser.json());
app.use(cors());

//Keep and ongoing connection with the database

const databaseConnection= async()=>{
    try{
        await mongoose.connect(connectioString,{useNewUrlParser:true,useUnifiedTopology:true});
        console.log('Database connected successfully');

    }catch(error){
        console.log('Database connection failed');
    }
    }


//Call the database connection function
databaseConnection();
//Start the server
app.listen(PORT,()=>console.log(`Server is running on port ${PORT}`));

// Routes

//test route
app.get('/test',(req,res)=>{
    res.send('Hello from the server');
}
);

