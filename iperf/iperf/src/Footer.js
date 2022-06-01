import React, { useState, useEffect } from "react";
import "./footer.css";

function Footer(props) {
  const [width, setwidth] = useState(props.width);
  useEffect(() => {
    setwidth(props.width);
  }, [props.width]);

  return <div className={width > 1080 ? "footer" : "footer-vertical"}></div>;
}

export default Footer;
