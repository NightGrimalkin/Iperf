import React, { useEffect, useState } from "react";
import { useSpring, animated as a } from "react-spring";
import CloseIcon from "@mui/icons-material/Close";
import SendIcon from "@mui/icons-material/Send";
import Login from "@mui/icons-material/Login";
import Footer from "./Footer.js";
import Main from "./Main.js";
import "./header.css";

function Header() {
  const corsURL = "http://localhost:8080";

  const [JWT, setJWT] = useState("");

  const [wasLoginSuccesfull, setWasLoginSuccesfull] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showLoginForm, setShowLoginForm] = useState(false);
  const [loginCredentials, setLoginCredentials] = useState({
    username: "",
    password: "",
  });

  const [width, setwidth] = useState(window.innerWidth);
  const handleResize = () => {
    setwidth(window.innerWidth);
  };
  useEffect(() => {
    window.addEventListener("resize", handleResize, false);
  });

  const loginFormAnim = useSpring({
    opacity: showLoginForm ? 1 : 0,
    pointerEvents: showLoginForm ? "all" : "none",
    marginTop: showLoginForm ? 0 : -500,
  });

  const handleLoginForm = (e) => {
    e.preventDefault();
    setLoginCredentials((prevState) => ({
      ...prevState,
      [e.target.name]: e.target.value,
    }));
  };

  const handleLoginFormSubmit = () => {
    let tempLoginCredentials = loginCredentials;
    fetch(corsURL + "/login", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams(tempLoginCredentials),
    })
      .then((data) => {
        return data.json();
      })
      .then((data) => {
        if (data.hasOwnProperty("login_error")) {
          setWasLoginSuccesfull("Błąd logowania: " + data.login_error);
          setLoginCredentials({
            username: "",
            password: "",
          });
          console.log(wasLoginSuccesfull);
        } else {
          setJWT("Bearer " + data);
          setShowLoginForm(false);
          setWasLoginSuccesfull("");
          setLoginCredentials({
            username: "",
            password: "",
          });
        }
      })
      .catch((error) => {
        setWasLoginSuccesfull("Błąd spróbuj ponownie");
        console.log(error);
      });
  };

  useEffect(() => {
    fetch(corsURL + "/api/token/refresh", {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((data) => {
        return data.json();
      })
      .then((data) => {
        if (typeof data === "object") {
          console.log(data.error_message);
        } else {
          setJWT("Bearer " + data);
        }
      })
      .catch((error) => {
        console.log(error);
      });
  }, []);

  return (
    <>
      <header className="header">
        <div
          className={
            showLoginForm ? "header-contents-disabled" : "header-contents"
          }
        >
          <button onClick={() => setShowLoginForm(true)}>
            <Login />
          </button>
          <h1>Iperf3 by UPC</h1>
          <p>Stan usługi: placeholder</p>
        </div>
        <a.div className="form-area" style={loginFormAnim}>
          <button
            onClick={() => setShowLoginForm(false)}
            className="close-button"
          >
            <CloseIcon />
          </button>

          <div className="inputs-area">
            {!wasLoginSuccesfull == "" ? (
              <div className="error-message">
                <p>{wasLoginSuccesfull}</p>
              </div>
            ) : (
              ""
            )}
            <p>Podaj swój login</p>
            <input
              type="text"
              name="username"
              value={loginCredentials.username}
              onChange={handleLoginForm}
            />
            <p>Podaj hasło</p>
            <input
              type="password"
              name="password"
              value={loginCredentials.password}
              onChange={handleLoginForm}
            />
            <p>Zaloguj</p>
            <button className="post-button" onClick={handleLoginFormSubmit}>
              <SendIcon />
            </button>
          </div>
        </a.div>
      </header>
      <Main loginFormActive={showLoginForm} jwt={JWT} width={width} />
      <Footer width={width} />
    </>
  );
}

export default Header;
