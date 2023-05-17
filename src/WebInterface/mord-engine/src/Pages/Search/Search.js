import React from "react";
import classes from "./search.module.css";
import logo from "../../assets/logo3.svg";
import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Search(props) {
    const navigate = useNavigate();
  const [inputValue, setInputValue] = useState("");
  const [suggestionList, setSuggestionList] = useState([]);

  const handleInputChange = (event) => {
    // console.log(event.target.value);
    setInputValue(event.target.value);
    // getSuggestionList();
  };

  const handleKeyPresssearch = (event) => {
    if (event.key === "Enter") {
      handleSubmit();
    }
  };

  const handleKeyPresssuggest = (item) => {
    setInputValue(item);
    handleSubmit();
  };

  const handleSubmit = () => {
    // Handle the submission logic here
    // console.log("Form submitted with value:", inputValue);
    props.setSearchValue(inputValue)
    // getSearchResult();
  };

  const getSearchResult = async () => {
    console.log(
      "search : https://localhost:3000/search?query=" + inputValue + "&limit=10"
    );
    try {
      const request = await axios.get(
        "https://localhost:3000/search?query=" + inputValue + "&limit=10"
      );
      props.setResponse(request.data);
      navigate("/results");
    } catch (err) {
      console.log("err");
    }
  };

  const getSuggestionList = async () => {
    console.log(
      "suggest : https://localhost:3000/suggestion?query=" + inputValue
    );
    try {
      const request = await axios.get(
        "https://localhost:3000/suggestion?query=" + inputValue
      );
      setSuggestionList(request.data);
    } catch (err) {
      console.log("err");
    }
  };

  return (
    <div className={classes.container}>
      <div className={classes.logoContainer}>
        <img src={logo} alt="Mord Search Enginr" />
      </div>
      <div className={classes.search}>
        {" "}
        <div className={classes.searchContainer}>
          <input
            className={
              suggestionList.length !== 0 ? classes.activeinput : classes.input
            }
            type="text"
            value={inputValue}
            onChange={handleInputChange}
            onKeyPress={handleKeyPresssearch}
            placeholder="Search MORD"
          />
        </div>
        {suggestionList.length !== 0 && (
          <div className={classes.suggest}>
            {suggestionList.map((item, index) => (
              <div
                className={classes.suggestitem}
                onClick={() => handleKeyPresssuggest(item)}>
                {item}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
