package com.example.csankaran.digitalsignin;

// Class for being able to initialize objects for storing data on database
public class UserInformation {
        private String name;
        private String time;
        private boolean signin;
        private boolean signout;
        private String date;

        public UserInformation() {

        }


        public UserInformation(String name, String Time, Boolean SignIn, Boolean SignOut, String date) {
            this.name = name;
            this.time = Time;
            this.signin = SignIn;
            this.signout = SignOut;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public boolean getSignin() {
            return signin;
        }

        public void setSignin(boolean signin) {
            this.signin = signin;
        }

        public boolean getSignout() {
            return signout;
        }

        public void setSignout(boolean signout) {
            this.signout = signout;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
}