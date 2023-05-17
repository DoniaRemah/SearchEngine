import React from "react";
import classes from "./singlecard.module.css";
import { Link } from "react-router-dom";

export default function SingleCard(props) {
  return (
    <div className={classes.container}>
      <a href={props.item.URL} target="_blank" className={classes.title}>
        {props.item.Title}
      </a>
      <a className={classes.url} href={props.item.URL} target="_blank">
        {props.item.URL}
      </a>
      <div className={classes.para}>{props.item.Content}</div>
    </div>
  );
}
