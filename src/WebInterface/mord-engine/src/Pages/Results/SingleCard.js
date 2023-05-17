import React from "react";
import classes from "./singlecard.module.css";
import { Link } from "react-router-dom";

export default function SingleCard(props) {
  const paragraph = props.item.Content;
  const substring = props.words;

  const highlightSubstring = () => {
    for (let index = 0; index < substring.length; index++) {
      const startIndex = paragraph.indexOf(substring[index]);
      if (startIndex !== -1) {
        const endIndex = startIndex + substring[index].length;
        return (
          <>
            {paragraph.substring(0, startIndex)}
            <strong>{paragraph.substring(startIndex, endIndex)}</strong>
            {paragraph.substring(endIndex)}
          </>
        );
      }
    }
    return paragraph;
  };

  return (
    <div className={classes.container}>
      <a href={props.item.URL} target="_blank" className={classes.title}>
        {props.item.Title}
      </a>
      <a className={classes.url} href={props.item.URL} target="_blank">
        {props.item.URL}
      </a>
      <p className={classes.para}>{highlightSubstring()}</p>
    </div>
  );
}
