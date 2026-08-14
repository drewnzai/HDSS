import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useLoginMutation } from "../store/AuthApi";

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [login, { isLoading, error }] = useLoginMutation();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await login({ username, password }).unwrap();
            navigate("/", { replace: true });
        } catch {
            // error state below already reflects the failure
        }
    };

    return (
        <div style={{ maxWidth: 320, margin: "4rem auto" }}>
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Username</label>
                    <input value={username} onChange={(e) => setUsername(e.target.value)} />
                </div>
                <div>
                    <label>Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button type="submit" disabled={isLoading}>
                    {isLoading ? "Logging in..." : "Login"}
                </button>
                {error && <p style={{ color: "red" }}>Login failed</p>}
            </form>
        </div>
    );
}

export default Login;