import React from "react";
import classes from "./singlecard.module.css";
import { Link } from "react-router-dom";

export default function SingleCard(props) {
  const paragraph = props.item.Content;
  const substrings = props.words;

  const highlightSubstring = () => {
    let formattedText = paragraph;

    for (let index = 0; index < substrings.length; index++) {
      const substring = substrings[index];
      const regex = new RegExp(`\\b${substring}\\b`, "gi");
      const matchFound = formattedText.match(regex);

      if (matchFound) {
        formattedText = formattedText.replace(
          regex,
          (match) => `<strong>${match}</strong>`
        );
      } else if (formattedText.includes(substring)) {
        formattedText = formattedText.replace(
          substring,
          `<strong>${substring}</strong>`
        );
      }
    }

    return { __html: formattedText };
  };

  return (
    <div className={classes.container}>
      <a href={props.item.URL} target="_blank" className={classes.title}>
        {props.item.Title}
      </a>
      <a className={classes.url} href={props.item.URL} target="_blank">
        {props.item.URL}
      </a>
      {/* <p className={classes.para}>{highlightSubstring()}</p> */}

      <p
        dangerouslySetInnerHTML={highlightSubstring()}
        className={classes.para}></p>
    </div>
  );
}
