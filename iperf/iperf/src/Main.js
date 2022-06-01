import React, { useEffect, useState } from "react";
import PulseLoader from "react-spinners/PulseLoader";
import { useTransition, useSpring, config, animated as a } from "react-spring";
import DOMPurify from "dompurify";
import "./main.css";

function Main(props) {
  const clientParam = {
    format: "-f [format to: Kbits, Mbits, KBytes, MBytes]",
    interval: "-i [seconds between reports]",
    verbose: "-V",
    json: "-J",
    debug: "-d",
  };
  const serverParam = {
    one_off: "-1",
    reverse: "-R",
    window: "-w  [set socket buffer size]",
    congestion:
      "-C [set TCP congestion control algorithm (Linux and FreeBSD only)]",
    set_mss: "-M [set TCP/SCTP maximum segment size (MTU - 40 bytes)]",
    no_delay: "-N",
    TOS: "-S [set the IP type of service]",
    version_4: "-4",
    version_6: "-6",
    flowlabel: "-L N",
    zerocopy: "-Z",
    omit: "-O [omit the first n seconds]",
    title_string: "-T [prefix every output line with this string]",
    get_server_output: "--get-server-output",
  };
  const corsURL = "http://localhost:8080";

  const [loadAnimation, setLoadAnimation] = useState(false);
  const [textAnimation, setTextAnimation] = useState(false);

  const [query, setQuery] = useState("ipconfig");
  const [queryResponse, setQueryResponse] = useState("");

  const [isLoggedIn, setIsLoggedIn] = useState(props.isLoggedIn);
  useEffect(() => {
    setIsLoggedIn(props.isLoggedIn);
  }, [props.isLoggedIn]);
  const [width, setwidth] = useState(props.width);
  useEffect(() => {
    setwidth(props.width);
  }, [props.width]);
  const [active, setActive] = useState(props.loginFormActive);
  useEffect(() => {
    setActive(props.loginFormActive);
  }, [props.loginFormActive]);
  const [JWT, setJwt] = useState(props.jwt);
  useEffect(() => {
    setJwt(props.jwt);
  }, [props.jwt]);

  const loaderAnim = useTransition(loadAnimation, {
    from: { opacity: 0 },
    enter: { opacity: 1 },
    config: config.molasses,
  });
  const textAnim = useSpring({
    pointerEvents: textAnimation ? "all" : "none",
    marginTop: textAnimation ? 0 : +600,
  });

  const queryBuilder = (e) => {
    e.preventDefault();
    let tempQuery = query;
    tempQuery = tempQuery + " " + e.target.value;
    setQuery(tempQuery);
  };
  const handleQueryChange = (e) => {
    setQuery(e.target.value);
  };
  const capitalizeFirstLetter = (string) => {
    return string.charAt(0).toUpperCase() + string.slice(1);
  };

  const validateUserInput = (userInput) => {
    userInput.startsWith("Iperf");
  };

  const handleQueryFormSubmit = (e) => {
    setQueryResponse(null);
    setTextAnimation(false);
    setLoadAnimation(true);
    let tempQuery = DOMPurify.sanitize(query);
    fetch(corsURL + "/iperf/ping", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: JWT,
      },
      body: tempQuery.toString(),
      credentials: "include",
    })
      .then((data) => {
        console.log(data);
        return data.json();
      })
      .then((data) => {
        console.log(data);
        if (data.hasOwnProperty("authorization_error")) {
          setQueryResponse(
            "Problem z autoryzacja: " +
              DOMPurify.sanitize(data.authorization_error)
          );
        } else if (data.hasOwnProperty("request_limit_error")) {
          setQueryResponse(
            "Przekroczono limit zapytań: " +
              DOMPurify.sanitize(data.request_limit_error)
          );
        } else {
          setQueryResponse(DOMPurify.sanitize(data));
        }
      })
      .catch((error) => {
        console.log(error);
        setQueryResponse("Wystąpił błąd: " + error);
      })
      .finally(() => {
        setLoadAnimation(false);
        setTextAnimation(true);
      });
  };

  return (
    <div
      className={
        width > 1080
          ? !active
            ? "query-area"
            : "query-area-disabled"
          : !active
          ? "query-area-zoom"
          : "query-area-disabled-zoom"
      }
    >
      <div className="query-builder-form">
        <textarea
          value={query}
          onChange={handleQueryChange}
          spellCheck="false"
        ></textarea>
        <button onClick={handleQueryFormSubmit} className="send-query">
          Wyślij zapytanie
        </button>
        <button onClick={() => setQuery("Iperf")} className="send-query">
          Wyczyść
        </button>
        <h2>Zapytania klienta</h2>
        <fieldset className="client">
          {Object.keys(clientParam).map((name, i) => (
            <button value={clientParam[name]} onClick={queryBuilder} key={i}>
              {name}
            </button>
          ))}
        </fieldset>
        <h2>Zapytania serwera</h2>
        <fieldset className="server">
          {Object.keys(serverParam).map((name, i) => (
            <button value={serverParam[name]} onClick={queryBuilder} key={i}>
              {capitalizeFirstLetter(name.replaceAll("_", " "))}
            </button>
          ))}
        </fieldset>
      </div>
      <div
        className={
          loadAnimation ? "query-result-div-loader" : "query-result-div"
        }
      >
        {loaderAnim(
          (styles, item) =>
            item && (
              <div className="loader">
                <a.div style={styles}>
                  <PulseLoader
                    color="#239a98"
                    loading={loadAnimation}
                    margin={30}
                    size={30}
                    speedMultiplier={0.6}
                  />
                </a.div>
              </div>
            )
        )}
        <a.pre style={textAnim} className="query-result">
          {queryResponse}
        </a.pre>
      </div>
    </div>
  );
}

export default Main;
