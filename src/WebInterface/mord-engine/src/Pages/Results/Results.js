import React from "react";
import logo from "../../assets/logo3.svg";
import axios from "axios";
import { useState, useEffect } from "react";
import classes from "./results.module.css";
import Loader from "../layout/Loader";
import { Link, useParams, useNavigate } from "react-router-dom";
import SingleCard from "./SingleCard";

export default function Results(props) {
  let { id } = useParams();
  const navigate = useNavigate();
  const [inputValue, setInputValue] = useState(id);
  const [page, setPage] = useState(1);
  const [suggestValue, setSuggestValue] = useState(id);
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
        setPage(1);
        navigate("/results/" + suggestValue);
      }
    }
  };

  const handleKeyPresssuggest = (item) => {
    // setInputValue(item);
    console.log(item);
    setInputValue(item);
    setPage(1);
    navigate("/results/" + item);
  };

  const getSuggestionList = async () => {
    console.log(
      "suggest : http://localhost:3001/suggestion?query=" + suggestValue
    );
    let check = /^\s*$/.test(suggestValue);
    if (!check) {
      try {
        const request = await axios.get(
          "http://localhost:3001/suggestion?query=" + suggestValue
        );
        setSuggestionList(request.data);
      } catch (err) {
        console.log("err");
      }
    } else {
      setSuggestionList([]);
    }
  };

  const getSearchResult = async () => {
    // console.log(inputValue);
    console.log(
      "search : http://localhost:3001/search?query=" +
        inputValue +
        "&page=" +
        page +
        "&limit=10"
    );

    let check = /^\s*$/.test(inputValue);
    if (!check) {
      setLoading(true);
      try {
        const request = await axios.get(
          "http://localhost:3001/search?query=" +
            inputValue +
            "&page=" +
            page +
            "&limit=10"
        );
        setSearchList(request.data);
        setLoading(false);
      } catch (err) {
        console.log("err");
      }
    }
  };

  useEffect(() => {
    getSearchResult();
  }, [inputValue, page]);

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
              Value={inputValue}
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
                  key={item + index}
                  onClick={() => handleKeyPresssuggest(item)}
                  className={classes.suggestitem}>
                  {item}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      <div className={classes.content}>
        {loading ? (
          <Loader color="#a5278d" />
        ) : (
          <div className={classes.result}>
            <div className={classes.time}>
              results {"(" + searchList.time + " seconds )"}{" "}
            </div>
            <div className={classes.cards}>
              {searchList.result.map((item, index) => (
                <SingleCard key={index} item={item} words={searchList.Words} />
              ))}
            </div>
            <div className={classes.pagination}>
              {searchList.pagination.previousPage !== null ? (
                <div onClick={() => setPage(page - 1)} className={classes.page}>
                  Previous
                </div>
              ) : null}
              {searchList.result.map((item, index) =>
                page + index <= searchList.pagination.totalPages ? (
                  <div
                    onClick={() => setPage(page + index)}
                    className={classes.page}>
                    {page + index}
                  </div>
                ) : null
              )}
              {searchList.pagination.nextPage !== null ? (
                <div onClick={() => setPage(page + 1)} className={classes.page}>
                  Next
                </div>
              ) : null}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
