import React from "react";
import logo from "../../assets/logo3.svg";
import axios from "axios";
import { useState, useEffect } from "react";
import classes from "./results.module.css";
import Loader from "../layout/Loader";
import { Link, useParams , useNavigate } from "react-router-dom";

export default function Results(props) {
  let { id } = useParams();
  const navigate = useNavigate();
  const [inputValue, setInputValue] = useState(id);
  const [suggestValue, setSuggestValue] = useState("");
  const [suggestionList, setSuggestionList] = useState([]);
  const [searchList, setSearchList] = useState([]);
  const [loading, setLoading] = useState(true);

  const handleInputChange = (event) => {
    // console.log(event.target.value);
    setSuggestValue(event.target.value);
  };

  const handleKeyPresssearch = (event) => {
    if (event.key === "Enter") {
      let check = /^\s*$/.test(suggestValue);
      if (suggestValue !== "" && !check) {
        setInputValue(suggestValue);
        navigate("/results/" + suggestValue);
      }
    }
  };

  const handleKeyPresssuggest = (item) => {
    setInputValue(item);
    navigate("/results/" + suggestValue);
  };

  const getSuggestionList = async () => {
    console.log(
      "suggest : https://localhost:3000/suggestion?query=" + suggestValue
    );
    let check = /^\s*$/.test(suggestValue);
    if (!check) {
      try {
        const request = await axios.get(
          "https://localhost:3000/suggestion?query=" + suggestValue
        );
        setSuggestionList(request.data);
      } catch (err) {
        console.log("err");
      }
    }
  };

  const getSearchResult = async () => {
    // console.log(inputValue);
    console.log(
      "search : https://localhost:3000/search?query=" + inputValue + "&limit=10"
    );

    let check = /^\s*$/.test(inputValue);
    if (!check) {
      try {
        const request = await axios.get(
          "https://localhost:3000/search?query=" + inputValue + "&limit=10"
        );
        setLoading(false);
      } catch (err) {
        console.log("err");
      }
    }
  };

  useEffect(() => {
    getSearchResult();
  }, [inputValue]);

  useEffect(() => {
    getSuggestionList();
  }, [suggestValue]);

  return (
    <div className={classes.container}>
      <div className={classes.header}>
        <Link to="/" className={classes.logoContainer}>
          <img src={logo} alt="Mord Search Engine" />
        </Link>
        <div className={classes.search}>
          <div className={classes.searchContainer}>
            <input
              className={
                suggestionList.length !== 0 ? classes.input : classes.ninput
              }
              type="text"
              defaultValue={inputValue}
              // value={suggestValue}
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
      <div className={classes.content}>
        {loading ? <Loader color="#a5278d" /> : null}
      </div>
    </div>
  );
}
