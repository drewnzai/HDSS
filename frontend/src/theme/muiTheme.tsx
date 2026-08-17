import { createTheme } from "@mui/material/styles";

export function getMuiTheme(mode: "light" | "dark") {
    return createTheme({
        palette: {
            mode,
            primary: {
                main: mode === "dark" ? "#2fb8c4" : "#0e7c86" // matches your accent tokens
            }
        },
        typography: {
            fontFamily: '"IBM Plex Sans", sans-serif'
        }
    });
}