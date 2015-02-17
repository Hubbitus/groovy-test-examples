class SecurityConfig extends ComposedConfigScript {         
    def run() { // normal contents of a config file go in here
        
        security {
            includeScript( SecurityDefaults )
            active = true
            password = 'redefined'
        }

    }
}
