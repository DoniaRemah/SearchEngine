import React from "react";
import logo from "../../assets/logo3.svg";
import axios from "axios";
import { useState, useEffect } from "react";
import classes from "./results.module.css";
import Loader from "../layout/Loader";
import { Link, useParams, useNavigate } from "react-router-dom";
import SingleCard from "./SingleCard";

export default function Results(props) {
  const arrow = (
    <svg x="0" y="0" viewBox="0 0 24 24">
      <path
        fill-rule="evenodd"
        clip-rule="evenodd"
        d="M13.8 7l-5 5 5 5 1.4-1.4-3.6-3.6 3.6-3.6z"></path>
    </svg>
  );
  let { id } = useParams();
  const navigate = useNavigate();
  const [inputValue, setInputValue] = useState(id);
  const [page, setPage] = useState(1);
  const [suggestValue, setSuggestValue] = useState("");
  const [suggestionList, setSuggestionList] = useState(["rana", "ola"]);
  const [searchList, setSearchList] = useState({
    result: [
      {
        URL: "https://www.southsideblooms.com/how-flowers-are-important-in-our-life/",
        Title: "How flowers are important in our life? - Southside Blooms",
        Content:
          "How flowers are important in our life? - Southside Blooms Skip to content Flower delivery Chicago Shop Our Story Volunteer Events Donate Weddings More Menu Toggle Press and Media Career Blog Contact us Login Main Menu Flower delivery Chicago Shop Our Story Volunteer Events Donate Weddings More Menu Toggle Press and Media Career Blog Contact us Login How flowers are important in our life? 1 Comment / Uncategorized / By Quilen Blackwell What is the importance of flowers in our life? Flowers not only add color, texture, and biodiversity to gardens and environments, they are also an important structure for p",
      },
      {
        URL: "https://www.southsideblooms.com/how-flowers-are-important-in-our-life/",
        Title: "How flowers are important in our life? - Southside Blooms",
        Content:
          "How flowers are important in our life? - Southside Blooms Skip to content Flower delivery Chicago Shop Our Story Volunteer Events Donate Weddings More Menu Toggle Press and Media Career Blog Contact us Login Main Menu Flower delivery Chicago Shop Our Story Volunteer Events Donate Weddings More Menu Toggle Press and Media Career Blog Contact us Login How flowers are important in our life? 1 Comment / Uncategorized / ",
      },
      {
        URL: "https://www.britannica.com/art/perfume",
        Title: "Perfume | Britannica",
        Content:
          "hy & Religion Politics, Law & Government Science Sports & Recreation Technology Visual Arts World History On This Day in History Quizzes Podcasts Dictionary Biographies Summaries Top Questions Infographics Demystified Lists #WTFact Companions Image Galleries Spotlight The Forum One Good Fact Entertainment & Pop Culture Geography & Travel Health & Medicine Lifestyles & Social Issues Literature Phil",
      },
      {
        URL: "https://www.recipegirl.com/how-to-make-iced-coffee/",
        Title: "How to Make Iced Coffee - Recipe Girl",
        Content:
          " caffeine needed. My body is hard-wired to have an abundance of natural energy from the moment I wake up. I guess I’m lucky that way. I do love the flavor of coffee though… coffee candies and coffee ice cream and even those foo foo frozen coffee drinks that contain your total allotted calorie consumption in just a dozen sips. That’s why on one rather sweltering afternoon recently, I grabbed my husband’s mug o’ coffee that had been sitting untouched on the counter all day long, and I made myself a rather delicious version of Iced Coffee. How do You Make Iced Coffee? You’ll need a tall glass and a spoon long enough to reach the bottom of that glass. Fill that glass full to the rim with ice. If you really want to get serious about your iced coffee, you can make ice cubes out of coffee too. Th",
      },
    ],
    pagination: {
      totalResults: 4,
      totalPages: 5,
      currentPage: 1,
      nextPage: 2,
      previousPage: null,
    },
    time: 0.448,
  });
  const [loading, setLoading] = useState(false);

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
    // setInputValue(item);
    console.log(item);
    setInputValue(suggestValue);
    navigate("/results/" + item);
  };

  const nextPage = () => {
    console.log("we are in next page");
  };

  // const getSuggestionList = async () => {
  //   console.log(
  //     "suggest : https://localhost:3000/suggestion?query=" + suggestValue
  //   );
  //   let check = /^\s*$/.test(suggestValue);
  //   if (!check) {
  //     try {
  //       const request = await axios.get(
  //         "https://localhost:3000/suggestion?query=" + suggestValue
  //       );
  //       setSuggestionList(request.data);
  //     } catch (err) {
  //       console.log("err");
  //     }
  //   }
  // };

  // const getSearchResult = async () => {
  //   // console.log(inputValue);
  //   console.log(
  //     "search : https://localhost:3000/search?query=" +
  //       inputValue +
  //       "&page=" +
  //       page +
  //       "&limit=10"
  //   );

  //   let check = /^\s*$/.test(inputValue);
  //   if (!check) {
  //     try {
  //       const request = await axios.get(
  //         "https://localhost:3000/search?query=" +
  //           inputValue +
  //           "&page=" +
  //           page +
  //           "&limit=10"
  //       );
  //       setLoading(false);
  //     } catch (err) {
  //       console.log("err");
  //     }
  //   }
  // };

  // useEffect(() => {
  //   getSearchResult();
  // }, [inputValue, page]);

  // useEffect(() => {
  //   getSuggestionList();
  // }, [suggestValue]);

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
                <Link key={item + index} to={`/results/${item}`}>
                  <div className={classes.suggestitem}>{item}</div>
                </Link>
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
                <SingleCard key={index} item={item} />
              ))}
            </div>
            <div className={classes.icons}>
              <span onClick={() => nextPage}>{arrow}</span>
              <span onClick={nextPage}>{arrow}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
