import React from "react";
import classes from "./search.module.css";
import logo from "../../assets/logo3.svg";
import { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";

export default function Search(props) {
  const navigate = useNavigate();
  const [inputValue, setInputValue] = useState("");
  const [suggestionList, setSuggestionList] = useState(["rana", "ola"]);

  const handleInputChange = (event) => {
    // console.log(event.target.value);
        setInputValue(event.target.value);
    let check = /^\s*$/.test(event.target.value);
    if (!check) {

    // getSuggestionList();
    }
  };

  const handleKeyPresssearch = (event) => {
    if (event.key === "Enter") {
      let check = /^\s*$/.test(inputValue);
      if (!check) {
        handleSubmit();
      }
    }
  };

  const handleSubmit = () => {
    // Handle the submission logic here
    // console.log("Form submitted with value:", inputValue);
    navigate("/results/" + inputValue);
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
              suggestionList.length !== 0 ? classes.input : classes.ninput
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
              <Link to={`/results/${item}`}>
                <div className={classes.suggestitem}>{item}</div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
